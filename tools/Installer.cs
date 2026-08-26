using System;
using System.Diagnostics;
using System.Drawing;
using System.Drawing.Drawing2D;
using System.IO;
using System.IO.Compression;
using System.Reflection;
using System.Runtime.InteropServices;
using System.Windows.Forms;

class Installer : Form
{
    [DllImport("user32.dll")]
    private static extern bool SetProcessDPIAware();
    [DllImport("user32.dll")]
    private static extern bool ReleaseCapture();
    [DllImport("user32.dll")]
    private static extern int SendMessage(IntPtr hWnd, int msg, int wParam, int lParam);

    Label titleLabel, statusLabel;
    TextBox pathBox;
    GradientButton actionButton;
    ProgressBar progress;
    float ui = 1.0f;

    string defaultDir = Path.Combine(
        Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData), "MoonLight");

    public Installer()
    {
        Text = "MoonLight Setup";
        FormBorderStyle = FormBorderStyle.None;
        StartPosition = FormStartPosition.CenterScreen;
        MaximizeBox = true;
        MinimizeBox = true;
        BackColor = Palette.Background;
        DoubleBuffered = true;

        var screen = Screen.PrimaryScreen.WorkingArea;
        ui = Math.Max(0.8f, Math.Min(screen.Height / 1080f * 1.1f, screen.Width / 1920f * 1.1f));

        int w = (int)(560 * ui), h = (int)(380 * ui);
        MinimumSize = new Size(w, h);
        Size = new Size(w, h);

        using (var path = new GraphicsPath())
        {
            int r = (int)(18 * ui);
            path.AddArc(0, 0, r, r, 180, 90);
            path.AddArc(w - r - 1, 0, r, r, 270, 90);
            path.AddArc(w - r - 1, h - r - 1, r, r, 0, 90);
            path.AddArc(0, h - r - 1, r, r, 90, 90);
            Region = new Region(path);
        }

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
        drag.Bounds = new Rectangle(0, 0, w, (int)(120 * ui));
        drag.Cursor = Cursors.SizeAll;
        drag.MouseDown += (s, e) => { if (e.Button == MouseButtons.Left) { ReleaseCapture(); SendMessage(Handle, 0xA1, 0x2, 0); } };
        Controls.Add(drag);
        drag.BringToFront();

        var logo = new Panel();
        logo.Size = new Size((int)(64 * ui), (int)(64 * ui));
        logo.Location = new Point(w / 2 - (int)(32 * ui), (int)(34 * ui));
        logo.Paint += (s, e) => UI.DrawCrescent(e.Graphics, logo.Width / 2, logo.Height / 2, (int)(26 * ui), Palette.Accent, Palette.Background);
        Controls.Add(logo);

        titleLabel = UI.TextLabel("Установка MoonLight", 0, (int)(110 * ui), 16f, true, Palette.Title);
        titleLabel.AutoSize = false;
        titleLabel.Size = new Size(w, (int)(34 * ui));
        titleLabel.TextAlign = ContentAlignment.MiddleCenter;
        Controls.Add(titleLabel);

        var sub = UI.TextLabel("Лаунчер и файлы игры будут установлены на компьютер", 0, (int)(146 * ui), 9f, false, Palette.Dim);
        sub.AutoSize = false;
        sub.Size = new Size(w, (int)(22 * ui));
        sub.TextAlign = ContentAlignment.MiddleCenter;
        Controls.Add(sub);

        var pathLabel = UI.TextLabel("ПАПКА УСТАНОВКИ", (int)(60 * ui), (int)(190 * ui), 7.5f, true, Palette.Dim);
        Controls.Add(pathLabel);

        pathBox = new TextBox();
        pathBox.Text = defaultDir;
        pathBox.BorderStyle = BorderStyle.FixedSingle;
        pathBox.BackColor = Palette.Surface;
        pathBox.ForeColor = Palette.Text;
        pathBox.Font = new Font("Segoe UI", 10f * ui);
        pathBox.Size = new Size(w - (int)(120 * ui), (int)(36 * ui));
        pathBox.Location = new Point((int)(60 * ui), (int)(212 * ui));
        Controls.Add(pathBox);

        actionButton = new GradientButton();
        actionButton.Text = "Установить";
        actionButton.Font = new Font("Segoe UI", 11f * ui, FontStyle.Bold);
        actionButton.Size = new Size(w - (int)(120 * ui), (int)(46 * ui));
        actionButton.Location = new Point((int)(60 * ui), (int)(262 * ui));
        actionButton.Cursor = Cursors.Hand;
        actionButton.Click += OnInstall;
        Controls.Add(actionButton);

        progress = new ProgressBar();
        progress.Size = new Size(w - (int)(120 * ui), (int)(10 * ui));
        progress.Location = new Point((int)(60 * ui), (int)(316 * ui));
        progress.Style = ProgressBarStyle.Continuous;
        progress.Visible = false;
        Controls.Add(progress);

        statusLabel = UI.TextLabel("", 0, (int)(334 * ui), 8.5f, false, Palette.Dim);
        statusLabel.AutoSize = false;
        statusLabel.Size = new Size(w, (int)(20 * ui));
        statusLabel.TextAlign = ContentAlignment.MiddleCenter;
        Controls.Add(statusLabel);
    }

    void OnInstall(object sender, EventArgs e)
    {
        try
        {
            string target = pathBox.Text.Trim();
            Directory.CreateDirectory(target);

            actionButton.Enabled = false;
            actionButton.Text = "Установка...";
            progress.Visible = true;
            progress.Value = 5;
            statusLabel.Text = "Распаковка файлов игры...";

            var steps = new Action[]
            {
                () => ExtractResource("MoonLight.launcher.exe", Path.Combine(target, "MoonLight Launcher.exe")),
                () => ExtractZip("MoonLight.game.zip", Path.Combine(target, "game")),
                () => CreateShortcut(Path.Combine(target, "MoonLight Launcher.exe"))
            };

            var timer = new Timer();
            timer.Interval = 10;
            int step = 0;

            timer.Tick += (a, b) =>
            {
                if (step < steps.Length)
                {
                    steps[step]();
                    step++;
                    progress.Value = 5 + step * 30;
                }
                else
                {
                    timer.Stop();
                    progress.Value = 100;
                    statusLabel.Text = "Готово! MoonLight установлен";
                    titleLabel.Text = "Установка завершена";
                    actionButton.Enabled = true;
                    actionButton.Text = "Запустить";
                    actionButton.Click -= OnInstall;
                    actionButton.Click += (s2, e2) =>
                    {
                        Process.Start(new ProcessStartInfo
                        {
                            FileName = Path.Combine(target, "MoonLight Launcher.exe"),
                            WorkingDirectory = target,
                            UseShellExecute = true
                        });
                        Close();
                    };
                }
            };
            timer.Start();
        }
        catch (Exception ex)
        {
            statusLabel.Text = "Ошибка: " + ex.Message;
            actionButton.Enabled = true;
            actionButton.Text = "Установить";
        }
    }

    void ExtractResource(string resourceName, string targetFile)
    {
        using (var stream = Assembly.GetExecutingAssembly().GetManifestResourceStream(resourceName))
        {
            if (stream == null) throw new Exception("ресурс не найден: " + resourceName);

            using (var file = File.Create(targetFile))
                stream.CopyTo(file);
        }
    }

    void ExtractZip(string resourceName, string targetDir)
    {
        Directory.CreateDirectory(targetDir);

        using (var stream = Assembly.GetExecutingAssembly().GetManifestResourceStream(resourceName))
        using (var archive = new ZipArchive(stream))
        {
            int total = archive.Entries.Count, done = 0;

            foreach (var entry in archive.Entries)
            {
                string targetPath = Path.Combine(targetDir, entry.FullName);

                if (string.IsNullOrEmpty(entry.Name))
                {
                    Directory.CreateDirectory(targetPath);
                    continue;
                }

                Directory.CreateDirectory(Path.GetDirectoryName(targetPath));
                using (var entryStream = entry.Open())
                using (var file = File.Create(targetPath))
                    entryStream.CopyTo(file);

                done++;
                if (done % 40 == 0)
                {
                    progress.Value = Math.Min(95, 35 + done * 60 / total);
                    statusLabel.Text = "Файлов: " + done + " / " + total;
                }
            }
        }
    }

    void CreateShortcut(string targetExe)
    {
        try
        {
            string desktop = Environment.GetFolderPath(Environment.SpecialFolder.DesktopDirectory);
            string shortcutPath = Path.Combine(desktop, "MoonLight Launcher.lnk");

            var shellType = Type.GetTypeFromProgID("WScript.Shell");
            dynamic shell = Activator.CreateInstance(shellType);
            var shortcut = shell.CreateShortcut(shortcutPath);
            shortcut.TargetPath = targetExe;
            shortcut.WorkingDirectory = Path.GetDirectoryName(targetExe);
            shortcut.Save();
        }
        catch { }
    }

    protected override void OnPaintBackground(PaintEventArgs e)
    {
        using (var brush = new LinearGradientBrush(ClientRectangle, Color.FromArgb(16, 16, 26), Color.FromArgb(28, 22, 48), 100f))
            e.Graphics.FillRectangle(brush, ClientRectangle);

        using (var glow = new SolidBrush(Color.FromArgb(16, Palette.Accent)))
            e.Graphics.FillEllipse(glow, -Width / 3, -Height / 3, Width * 1.3f, Height);
    }

    [STAThread]
    static void Main()
    {
        SetProcessDPIAware();
        Application.EnableVisualStyles();
        Application.Run(new Installer());
    }
}
