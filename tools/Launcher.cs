using System;
using System.Diagnostics;
using System.Drawing;
using System.Drawing.Drawing2D;
using System.IO;
using System.Runtime.InteropServices;
using System.Windows.Forms;

partial class Launcher : Form
{
    [DllImport("user32.dll")]
    private static extern bool SetProcessDPIAware();
    [DllImport("user32.dll")]
    private static extern bool ReleaseCapture();
    [DllImport("user32.dll")]
    private static extern int SendMessage(IntPtr hWnd, int msg, int wParam, int lParam);

    string[] navItems = { "Главная", "Версия", "Аккаунт", "Мастерская", "Новости", "Настройки" };
    int activeNav;
    int hoverNav = -1;
    internal string[][] settingsRows;

    Panel sidebar, logOverlay;
    GradientButton play;
    TextBox heroNick, accountNick, log;
    Label greeting, statusLine;
    string currentNick = "moonlight";
    bool logVisible;
    float ui = 1.0f;

    int baseW = 1000, baseH = 640;
    int sideW = 220, topH = 60;

    public Launcher()
    {
        Text = "MoonLight Launcher";
        FormBorderStyle = FormBorderStyle.None;
        StartPosition = FormStartPosition.CenterScreen;
        MaximizeBox = false;
        BackColor = Palette.Background;
        DoubleBuffered = true;

        var screen = Screen.PrimaryScreen.WorkingArea;
        ui = Math.Max(0.65f, Math.Min(screen.Height / 1080f * 1.25f, screen.Width / 1920f * 1.25f));

        int w = (int)(baseW * ui), h = (int)(baseH * ui);
        MinimumSize = new Size(w, h);
        Size = new Size(w, h);
        Icon = LoadIcon();

        using (var path = new GraphicsPath())
        {
            int r = (int)(20 * ui);
            path.AddArc(0, 0, r, r, 180, 90);
            path.AddArc(w - r - 1, 0, r, r, 270, 90);
            path.AddArc(w - r - 1, h - r - 1, r, r, 0, 90);
            path.AddArc(0, h - r - 1, r, r, 90, 90);
            Region = new Region(path);
        }

        int sideWU = (int)(sideW * ui), topHU = (int)(topH * ui);

        sidebar = new Panel();
        sidebar.Bounds = new Rectangle(0, 0, sideWU, h);
        sidebar.Paint += PaintSidebar;
        sidebar.MouseMove += SidebarMouseMove;
        sidebar.MouseLeave += (s, e) => { hoverNav = -1; sidebar.Invalidate(); };
        sidebar.MouseDown += SidebarMouseDown;
        Controls.Add(sidebar);

        greeting = UI.TextLabel("С возвращением, moonlight!", sideWU + (int)(20 * ui), (int)(19 * ui), 11f, true, Palette.Title);
        Controls.Add(greeting);

        var logChip = new Label();
        logChip.Text = "ЛОГ";
        logChip.ForeColor = Palette.Text;
        logChip.Font = new Font("Segoe UI", 8f * ui, FontStyle.Bold);
        logChip.AutoSize = false;
        logChip.Size = new Size((int)(56 * ui), (int)(26 * ui));
        logChip.Location = new Point(w - (int)(160 * ui), (int)(17 * ui));
        logChip.TextAlign = ContentAlignment.MiddleCenter;
        logChip.Cursor = Cursors.Hand;
        logChip.Paint += (s, e) => UI.PaintCard(e.Graphics, new Rectangle(0, 0, logChip.Width - 1, logChip.Height - 1), (int)(8 * ui), Palette.Surface, Color.FromArgb(50, 50, 68));
        logChip.Click += (s, e) => ToggleLog();
        Controls.Add(logChip);

        var minimize = new Label();
        minimize.Text = "—";
        minimize.ForeColor = Palette.Dim;
        minimize.Font = new Font("Segoe UI", 11f * ui, FontStyle.Bold);
        minimize.AutoSize = true;
        minimize.Cursor = Cursors.Hand;
        minimize.Location = new Point(w - (int)(86 * ui), (int)(16 * ui));
        minimize.Click += (s, e) => WindowState = FormWindowState.Minimized;
        Controls.Add(minimize);

        var close = new Label();
        close.Text = "X";
        close.ForeColor = Palette.Dim;
        close.Font = new Font("Segoe UI", 11f * ui, FontStyle.Bold);
        close.AutoSize = true;
        close.Cursor = Cursors.Hand;
        close.Location = new Point(w - (int)(48 * ui), (int)(16 * ui));
        close.Click += (s, e) => Close();
        close.MouseEnter += (s, e) => close.ForeColor = Palette.Error;
        close.MouseLeave += (s, e) => close.ForeColor = Palette.Dim;
        Controls.Add(close);

        var drag = new Label();
        drag.Text = "";
        drag.Bounds = new Rectangle(sideWU, 0, w - sideWU, topHU);
        drag.Cursor = Cursors.SizeAll;
        drag.MouseDown += (s, e) => { if (e.Button == MouseButtons.Left) { ReleaseCapture(); SendMessage(Handle, 0xA1, 0x2, 0); } };
        Controls.Add(drag);
        drag.BringToFront();

        int cx = sideWU + (int)(20 * ui), cw = w - sideWU - (int)(40 * ui);
        int cy = topHU + (int)(10 * ui), ch = h - cy - (int)(16 * ui);

        BuildPages(cx, cy, cw, ch);

        SetDoubleBuffer(sidebar);
        SetDoubleBuffer(logOverlay);
        foreach (var page in new[] { pageHome, pageVersions, pageAccount, pageWorkshop, pageNews, pageSettings })
            SetDoubleBuffer(page);

        logOverlay = new Panel();
        logOverlay.Bounds = new Rectangle(cx, cy, cw, ch);
        logOverlay.Visible = false;
        logOverlay.Paint += (s, e) => UI.PaintCard(e.Graphics, logOverlay.ClientRectangle, (int)(14 * ui), Color.FromArgb(13, 13, 19), Color.FromArgb(50, 50, 68));
        Controls.Add(logOverlay);
        logOverlay.BringToFront();

        log = new TextBox();
        log.Multiline = true;
        log.ReadOnly = true;
        log.ScrollBars = ScrollBars.None;
        log.BorderStyle = BorderStyle.None;
        log.BackColor = Color.FromArgb(13, 13, 19);
        log.ForeColor = Palette.Text;
        log.Font = new Font("Consolas", 8.25f * ui);
        log.Size = new Size(logOverlay.Width - (int)(20 * ui), logOverlay.Height - (int)(20 * ui));
        log.Location = new Point((int)(10 * ui), (int)(10 * ui));
        logOverlay.Controls.Add(log);

        AppendLog("[moonlight] лаунчер готов");

        ShowPage(0);
        SyncNick("moonlight", null);
    }

    void PaintSidebar(object sender, PaintEventArgs e)
    {
        var g = e.Graphics;
        g.SmoothingMode = SmoothingMode.AntiAlias;

        using (var fill = new SolidBrush(Color.FromArgb(20, 20, 28)))
            g.FillRectangle(fill, sidebar.ClientRectangle);
        using (var pen = new Pen(Color.FromArgb(38, 38, 52)))
            g.DrawLine(pen, sidebar.Width - 1, 0, sidebar.Width - 1, sidebar.Height);

        UI.DrawCrescent(g, (int)(30 * ui), (int)(30 * ui), (int)(11 * ui), Palette.Accent, Color.FromArgb(20, 20, 28));
        TextRenderer.DrawText(g, "Moonlight", new Font("Segoe UI", 12f * ui, FontStyle.Bold),
            new Point((int)(48 * ui), (int)(20 * ui)), Palette.Title);

        int navY = (int)(70 * ui);

        for (int i = 0; i < navItems.Length; i++)
        {
            var rect = new Rectangle((int)(14 * ui), navY, sidebar.Width - (int)(28 * ui), (int)(38 * ui));

            if (i == activeNav)
            {
                using (var path = UI.Round(rect, (int)(11 * ui)))
                using (var fill = new SolidBrush(Palette.Accent))
                    g.FillPath(fill, path);
            }
            else if (i == hoverNav)
            {
                using (var path = UI.Round(rect, (int)(11 * ui)))
                using (var fill = new SolidBrush(Palette.SurfaceLight))
                    g.FillPath(fill, path);
            }

            var icon = new Rectangle(rect.X + (int)(12 * ui), rect.Y + rect.Height / 2 - (int)(6 * ui), (int)(12 * ui), (int)(12 * ui));
            using (var path = UI.Round(icon, (int)(4 * ui)))
            using (var fill = new SolidBrush(i == activeNav ? Color.White : Palette.Dim))
                g.FillPath(fill, path);

            TextRenderer.DrawText(g, navItems[i], new Font("Segoe UI", 9.5f * ui, i == activeNav ? FontStyle.Bold : FontStyle.Regular),
                new Point(rect.X + (int)(34 * ui), rect.Y + rect.Height / 2 - (int)(9 * ui)),
                i == activeNav ? Color.White : Palette.Text);

            navY += (int)(46 * ui);
        }

        int cardH = (int)(76 * ui);
        var card = new Rectangle((int)(14 * ui), sidebar.Height - cardH - (int)(14 * ui), sidebar.Width - (int)(28 * ui), cardH);
        UI.PaintCard(g, card, (int)(14 * ui), Palette.Surface, Color.FromArgb(45, 45, 62));

        var avatar = new Rectangle(card.X + (int)(12 * ui), card.Y + (int)(12 * ui), (int)(40 * ui), (int)(40 * ui));
        using (var path = UI.Round(avatar, (int)(12 * ui)))
        using (var fill = new SolidBrush(Palette.Accent))
            g.FillPath(fill, path);
        TextRenderer.DrawText(g, "M", new Font("Segoe UI", 14f * ui, FontStyle.Bold), avatar, Color.White,
            TextFormatFlags.HorizontalCenter | TextFormatFlags.VerticalCenter);

        TextRenderer.DrawText(g, currentNick, new Font("Segoe UI", 9.5f * ui, FontStyle.Bold),
            new Point(avatar.Right + (int)(10 * ui), card.Y + (int)(12 * ui)), Palette.Title);
        TextRenderer.DrawText(g, "UID 0001", new Font("Segoe UI", 7.5f * ui),
            new Point(avatar.Right + (int)(10 * ui), card.Y + (int)(32 * ui)), Palette.Dim);

        var chip = new Rectangle(avatar.Right + (int)(10 * ui), card.Y + (int)(48 * ui), (int)(62 * ui), (int)(18 * ui));
        using (var path = UI.Round(chip, (int)(9 * ui)))
        using (var fill = new SolidBrush(Palette.Success))
            g.FillPath(fill, path);
        TextRenderer.DrawText(g, "Player", new Font("Segoe UI", 7f * ui, FontStyle.Bold), chip, Color.White,
            TextFormatFlags.HorizontalCenter | TextFormatFlags.VerticalCenter);
    }

    void SidebarMouseMove(object sender, MouseEventArgs e)
    {
        int index = NavAt(e.Y);
        if (index != hoverNav) { hoverNav = index; sidebar.Invalidate(); }
    }

    void SidebarMouseDown(object sender, MouseEventArgs e)
    {
        int index = NavAt(e.Y);
        if (index >= 0) ShowPage(index);
    }

    int NavAt(int y)
    {
        int navY = (int)(70 * ui);

        for (int i = 0; i < navItems.Length; i++)
        {
            if (y >= navY && y <= navY + (int)(38 * ui)) return i;
            navY += (int)(46 * ui);
        }

        return -1;
    }

    void ToggleLog()
    {
        logVisible = !logVisible;
        logOverlay.Visible = logVisible;
    }

    void AppendLog(string line)
    {
        if (log.InvokeRequired) { log.BeginInvoke((Action)(() => AppendLog(line))); return; }
        if (line == null) return;

        log.AppendText(line + Environment.NewLine);
        log.SelectionStart = log.TextLength;
        log.ScrollToCaret();
    }

    void OnPlay(object sender, EventArgs e)
    {
        string dir = FindGameDir();

        if (dir == null)
        {
            AppendLog("[ошибка] не найден ни game/run-command.txt, ни папка проекта");
            statusLine.Text = "Игра не найдена";
            return;
        }

        string nickValue = heroNick.Text.Trim();
        if (nickValue.Length == 0) nickValue = "moonlight";

        try
        {
            string runDir = File.Exists(Path.Combine(dir, "run-command.txt"))
                ? Path.Combine(dir, "run")
                : Path.Combine(dir, "run");

            string configDir = Path.Combine(runDir, "moonlight");
            Directory.CreateDirectory(configDir);
            File.WriteAllText(Path.Combine(configDir, "launcher-nick.txt"), nickValue);
            AppendLog("[moonlight] ник: " + nickValue);
        }
        catch { }

        if (GetSetting("autoLog") == "1" && !logVisible) ToggleLog();

        try
        {
            string discordFile = Path.Combine(dir, "run", "moonlight", "discord-enabled.txt");
            Directory.CreateDirectory(Path.GetDirectoryName(discordFile));

            if (GetSetting("discordRpc") == "1") File.WriteAllText(discordFile, "1");
            else if (File.Exists(discordFile)) File.Delete(discordFile);
        }
        catch { }

        play.Enabled = false;
        play.Text = "Запуск...";
        statusLine.Text = "Запуск игры...";
        AppendLog("[moonlight] запуск");

        Process p;
        try { p = StartGame(dir); }
        catch (Exception ex)
        {
            AppendLog("[ошибка] " + ex.Message);
            statusLine.Text = "Ошибка запуска";
            play.Enabled = true;
            play.Text = "Играть";
            return;
        }

        p.OutputDataReceived += (s, a) => AppendLog(a.Data);
        p.ErrorDataReceived += (s, a) => AppendLog(a.Data);
        p.BeginOutputReadLine();
        p.BeginErrorReadLine();

        var t = new Timer();
        t.Interval = 1000;
        t.Tick += (a, b) =>
        {
            if (p.HasExited)
            {
                t.Stop();
                play.Enabled = true;
                play.Text = "Играть";
                statusLine.Text = p.ExitCode == 0 ? "Игра закрыта" : "Ошибка (код " + p.ExitCode + ")";
                AppendLog("[moonlight] процесс завершён, код " + p.ExitCode);
            }
        };
        t.Start();
    }

    string FindGameDir()
    {
        string portable = Path.Combine(Application.StartupPath, "game", "run-command.txt");
        if (File.Exists(portable))
            return Path.Combine(Application.StartupPath, "game");

        string dir = Application.StartupPath;
        for (int i = 0; i < 5 && dir != null; i++)
        {
            if (File.Exists(Path.Combine(dir, "gradlew.bat")))
                return dir;
            dir = Path.GetDirectoryName(dir);
        }

        return null;
    }

    string FindJava()
    {
        string[] candidates =
        {
            Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.UserProfile), ".jdks"),
            "C:\\Program Files\\Eclipse Adoptium",
            "C:\\Program Files\\Java",
            "C:\\Program Files\\Microsoft"
        };

        string best = null;

        foreach (string root in candidates)
        {
            if (!Directory.Exists(root)) continue;

            try
            {
                foreach (var dir in Directory.GetDirectories(root))
                {
                    string java = Path.Combine(dir, "bin", "java.exe");
                    if (!File.Exists(java)) continue;
                    if (dir.Contains("21")) return java;
                    if (best == null) best = java;
                }
            }
            catch { }
        }

        return best ?? "java";
    }

    Process StartGame(string dir)
    {
        var psi = new ProcessStartInfo();
        psi.UseShellExecute = false;
        psi.CreateNoWindow = GetSetting("hideConsole") == "1";
        psi.RedirectStandardOutput = GetSetting("hideConsole") == "1";
        psi.RedirectStandardError = GetSetting("hideConsole") == "1";

        string cmdFile = Path.Combine(dir, "run-command.txt");

        if (File.Exists(cmdFile) && GetSetting("fastLaunch") == "1")
        {
            string mainClass = "", classpath = "", workingDir = dir, javaExec = "{{JAVA}}", jvmArgs = "", args = "";

            foreach (string line in File.ReadAllLines(cmdFile))
            {
                int idx = line.IndexOf('=');
                if (idx <= 0) continue;

                string key = line.Substring(0, idx);
                string value = line.Substring(idx + 1);

                if (key == "mainClass") mainClass = value;
                else if (key == "classpath") classpath = value;
                else if (key == "workingDir") workingDir = value;
                else if (key == "javaExec") javaExec = value;
                else if (key == "jvmArgs") jvmArgs = value;
                else if (key == "args") args = value;
            }

            string baseDir = Application.StartupPath;
            classpath = classpath.Replace("CLASSES", Path.Combine(dir, "classes"));

            var sb = new System.Text.StringBuilder();
            foreach (string entry in classpath.Split(';'))
            {
                string resolved = entry;
                if (entry.StartsWith("LIB/"))
                    resolved = Path.Combine(dir, "libs", entry.Substring(4));
                resolved = resolved.Replace("{{BASE}}", baseDir);
                if (sb.Length > 0) sb.Append(';');
                sb.Append(resolved);
            }
            classpath = sb.ToString();

            workingDir = workingDir.Replace("{{BASE}}", baseDir);
            Directory.CreateDirectory(workingDir);
            Directory.CreateDirectory(Path.Combine(workingDir, "assets"));

            if (javaExec == "{{JAVA}}") javaExec = FindJava();
            javaExec = javaExec.Replace("{{BASE}}", baseDir);

            jvmArgs = jvmArgs.Replace("{{BASE}}", baseDir);
            args = args.Replace("{{BASE}}", baseDir);

            string arguments = "";
            foreach (string a in jvmArgs.Split(new[] { "||" }, StringSplitOptions.RemoveEmptyEntries))
                arguments += Quote(a) + " ";
            arguments += "-cp " + Quote(classpath) + " " + mainClass;
            foreach (string a in args.Split(new[] { "||" }, StringSplitOptions.RemoveEmptyEntries))
                arguments += " " + Quote(a);

            psi.FileName = javaExec;
            psi.Arguments = arguments;
            psi.WorkingDirectory = workingDir;
            AppendLog("[moonlight] быстрый запуск");
        }
        else
        {
            psi.FileName = "cmd.exe";
            psi.Arguments = "/c gradlew.bat runClient exportRunCommand --console=plain";
            psi.WorkingDirectory = dir;
            AppendLog("[gradle] первая сборка... следующие запуски будут мгновенными");
        }

        return Process.Start(psi);
    }

    static string Quote(string s)
    {
        if (s == null) return "\"\"";
        if (s.Contains(" ") && !s.StartsWith("\"")) return "\"" + s + "\"";
        return s;
    }

    Icon LoadIcon()
    {
        try
        {
            return Icon.ExtractAssociatedIcon(Application.ExecutablePath);
        }
        catch { }

        var bitmap = new Bitmap(32, 32);
        using (var g = Graphics.FromImage(bitmap))
        {
            g.SmoothingMode = SmoothingMode.AntiAlias;
            UI.DrawCrescent(g, 16, 16, 14, Palette.Accent, Color.FromArgb(17, 17, 23));
        }
        return Icon.FromHandle(bitmap.GetHicon());
    }

    static void SetDoubleBuffer(Control control)
    {
        typeof(Control).GetProperty("DoubleBuffered",
            System.Reflection.BindingFlags.Instance | System.Reflection.BindingFlags.NonPublic | System.Reflection.BindingFlags.SetProperty)
            .SetValue(control, true, null);
    }

    protected override void OnPaintBackground(PaintEventArgs e)
    {
        using (var brush = new SolidBrush(Palette.Background))
            e.Graphics.FillRectangle(brush, ClientRectangle);
    }

    [STAThread]
    static void Main()
    {
        SetProcessDPIAware();
        Application.EnableVisualStyles();
        Application.Run(new Launcher());
    }
}
