using System;
using System.Drawing;
using System.Drawing.Drawing2D;
using System.Windows.Forms;

partial class Launcher
{
    Panel pageHome, pageVersions, pageAccount, pageWorkshop, pageNews, pageSettings;

    string[][] newsData =
    {
        new[] { "Обновление", "Обновление клиента v1.0", "Новое меню, ClickGUI и музыкальный плеер", "Сегодня" },
        new[] { "Ново", "Новые аддоны", "Music Bar, Hot Keys и Particles уже в клиенте", "Вчера" },
        new[] { "Скоро", "Discord RPC", "Кастомный статус для Discord — укажи client id", "Скоро" },
        new[] { "Фикс", "Исправления", "Клики меню, шейдеры bloom и зоны кнопок", "Вчера" },
        new[] { "Обновление", "Лаунчер 2.0", "Быстрый запуск игры без пересборки", "Сегодня" }
    };

    string[][] addonsData =
    {
        new[] { "Full Bright", "H" },
        new[] { "Music Bar", "Q" },
        new[] { "Hot Keys", "N" },
        new[] { "Logotype", "M" },
        new[] { "Particles", "P" },
        new[] { "Target Info", "-" }
    };

    void BuildPages(int x, int y, int cw, int ch)
    {
        pageHome = new Panel();
        pageVersions = new Panel();
        pageAccount = new Panel();
        pageWorkshop = new Panel();
        pageNews = new Panel();
        pageSettings = new Panel();

        var pages = new[] { pageHome, pageVersions, pageAccount, pageWorkshop, pageNews, pageSettings };
        foreach (var page in pages)
        {
            page.Bounds = new Rectangle(x, y, cw, ch);
            page.BackColor = Palette.Background;
            Controls.Add(page);
        }

        BuildHome();
        BuildVersions();
        BuildAccount();
        BuildWorkshop();
        BuildNewsPage();
        BuildSettings();
    }

    void BuildHome()
    {
        int heroH = (int)(280 * ui);

        pageHome.Paint += (s, e) =>
        {
            var g = e.Graphics;
            g.SmoothingMode = SmoothingMode.AntiAlias;

            var hero = new Rectangle(0, 0, pageHome.Width - 1, heroH);
            using (var path = UI.Round(hero, (int)(18 * ui)))
            {
                using (var brush = new LinearGradientBrush(hero, Color.FromArgb(34, 24, 66), Palette.Background, 115f))
                    g.FillPath(brush, path);

                var clip = g.Clip;
                g.SetClip(path);

                using (var glow = new SolidBrush(Color.FromArgb(42, Palette.Accent)))
                    g.FillEllipse(glow, pageHome.Width - (int)(300 * ui), (int)(-120 * ui), (int)(520 * ui), (int)(520 * ui));

                var rnd = new Random(1337);
                using (var px = new SolidBrush(Color.FromArgb(80, Palette.Accent)))
                    for (int i = 0; i < 30; i++)
                    {
                        int size = (int)((2 + rnd.Next(4)) * ui);
                        g.FillRectangle(px, (int)(pageHome.Width * 0.5f) + rnd.Next((int)(pageHome.Width * 0.45f)),
                            rnd.Next(heroH), size, size);
                    }

                float mcx = pageHome.Width - (int)(120 * ui), mcy = heroH * 0.5f, mr = (int)(74 * ui);
                using (var halo = new SolidBrush(Color.FromArgb(60, Palette.Accent)))
                    g.FillEllipse(halo, mcx - mr - (int)(18 * ui), mcy - mr - (int)(18 * ui), (mr + (int)(18 * ui)) * 2, (mr + (int)(18 * ui)) * 2);
                UI.DrawCrescent(g, mcx, mcy, mr, Palette.Accent, Color.FromArgb(26, 20, 48));

                g.Clip = clip;
            }

            TextRenderer.DrawText(g, "Добро пожаловать", new Font("Segoe UI", 17f * ui, FontStyle.Bold),
                new Point((int)(30 * ui), (int)(26 * ui)), Palette.Title);
            TextRenderer.DrawText(g, "в Moonlight", new Font("Segoe UI", 17f * ui, FontStyle.Bold),
                new Point((int)(30 * ui), (int)(26 + 34) + (int)(6 * ui)), Palette.Accent);
            TextRenderer.DrawText(g, "Твой свет, твои правила", new Font("Segoe UI", 9f * ui),
                new Point((int)(32 * ui), (int)(108 * ui)), Palette.Dim);

            int by = heroH + (int)(14 * ui), bh = pageHome.Height - by;
            int nw = (int)(pageHome.Width * 0.56f);

            var newsRect = new Rectangle(0, by, nw - (int)(8 * ui), bh);
            UI.PaintCard(g, newsRect, (int)(16 * ui), Palette.Surface, Color.Empty);
            TextRenderer.DrawText(g, "Новости", new Font("Segoe UI", 10.5f * ui, FontStyle.Bold),
                new Point((int)(18 * ui), by + (int)(12 * ui)), Palette.Title);
            TextRenderer.DrawText(g, "Показать все", new Font("Segoe UI", 8f * ui),
                new Point(nw - (int)(96 * ui), by + (int)(16 * ui)), Palette.Dim);

            for (int i = 0; i < 3; i++)
                DrawNewsRow(g, nw, by, i, (int)(18 * ui), 0);

            var addRect = new Rectangle(nw + (int)(8 * ui), by, pageHome.Width - nw - (int)(8 * ui), bh);
            UI.PaintCard(g, addRect, (int)(16 * ui), Palette.Surface, Color.Empty);
            TextRenderer.DrawText(g, "Аддоны", new Font("Segoe UI", 10.5f * ui, FontStyle.Bold),
                new Point(nw + (int)(26 * ui), by + (int)(12 * ui)), Palette.Title);

            for (int i = 0; i < 4 && i < addonsData.Length; i++)
                DrawAddonRow(g, nw, by, i, nw + (int)(26 * ui));
        };

        var nickLabel = UI.TextLabel("НИКНЕЙМ", (int)(30 * ui), (int)(136 * ui), 7.5f, true, Palette.Dim);
        pageHome.Controls.Add(nickLabel);

        var nickBox = new Panel();
        nickBox.Size = new Size((int)(240 * ui), (int)(44 * ui));
        nickBox.Location = new Point((int)(28 * ui), (int)(158 * ui));
        nickBox.Paint += (s, e) =>
        {
            e.Graphics.SmoothingMode = SmoothingMode.AntiAlias;
            var rect = new Rectangle(0, 0, nickBox.Width - 1, nickBox.Height - 1);
            using (var p = UI.Round(rect, (int)(12 * ui)))
            using (var fill = new SolidBrush(Palette.Text))
            using (var pen = new Pen(heroNick.Focused ? Palette.Accent : Color.FromArgb(50, 50, 68), heroNick.Focused ? 1.6f : 1f))
            {
                e.Graphics.FillPath(fill, p);
                e.Graphics.DrawPath(pen, p);
            }
            TextRenderer.DrawText(e.Graphics, "@", new Font("Segoe UI", 11f * ui, FontStyle.Bold),
                new Rectangle((int)(12 * ui), 0, (int)(24 * ui), nickBox.Height),
                Color.FromArgb(90, 90, 115), TextFormatFlags.VerticalCenter | TextFormatFlags.HorizontalCenter);
        };
        pageHome.Controls.Add(nickBox);

        heroNick = new TextBox();
        heroNick.Text = "moonlight";
        heroNick.BorderStyle = BorderStyle.None;
        heroNick.BackColor = Palette.Text;
        heroNick.ForeColor = Color.Black;
        heroNick.Font = new Font("Segoe UI", 11.5f * ui);
        heroNick.Size = new Size(nickBox.Width - (int)(52 * ui), (int)(24 * ui));
        heroNick.Location = new Point((int)(46 * ui), (int)(11 * ui));
        heroNick.GotFocus += (s, e) => { heroNick.SelectionStart = heroNick.TextLength; heroNick.SelectionLength = 0; nickBox.Invalidate(); };
        heroNick.LostFocus += (s, e) => nickBox.Invalidate();
        heroNick.TextChanged += (s, e) => SyncNick(heroNick.Text);
        nickBox.Controls.Add(heroNick);

        play = new GradientButton();
        play.Text = "Играть";
        play.Font = new Font("Segoe UI", 11f * ui, FontStyle.Bold);
        play.Size = new Size((int)(150 * ui), (int)(44 * ui));
        play.Location = new Point((int)(282 * ui), (int)(158 * ui));
        play.Cursor = Cursors.Hand;
        play.Click += OnPlay;
        pageHome.Controls.Add(play);

        var versionChip = new Label();
        versionChip.Text = "Release 1.21.4";
        versionChip.ForeColor = Palette.Text;
        versionChip.Font = new Font("Segoe UI", 9.5f * ui);
        versionChip.AutoSize = false;
        versionChip.Size = new Size((int)(150 * ui), (int)(44 * ui));
        versionChip.Location = new Point((int)(444 * ui), (int)(158 * ui));
        versionChip.TextAlign = ContentAlignment.MiddleCenter;
        versionChip.Paint += (s, e) => UI.PaintCard(e.Graphics, new Rectangle(0, 0, versionChip.Width - 1, versionChip.Height - 1), (int)(12 * ui), Palette.SurfaceLight, Color.FromArgb(50, 50, 68));
        pageHome.Controls.Add(versionChip);

        statusLine = UI.TextLabel("", (int)(30 * ui), (int)(214 * ui), 8.5f, false, Palette.Dim);
        pageHome.Controls.Add(statusLine);
    }

    void DrawNewsRow(Graphics g, int nw, int by, int i, int x, int extraY)
    {
        int ry = by + (int)(46 * ui) + i * (int)(56 * ui) + extraY;
        var chipRect = new Rectangle(x, ry, (int)(82 * ui), (int)(19 * ui));

        using (var path = UI.Round(chipRect, (int)(9 * ui)))
        using (var fill = new SolidBrush(i == 0 ? Palette.Accent : i == 1 ? Palette.Success : Color.FromArgb(60, 60, 80)))
            g.FillPath(fill, path);
        TextRenderer.DrawText(g, newsData[i][0], new Font("Segoe UI", 7f * ui, FontStyle.Bold), chipRect, Color.White,
            TextFormatFlags.HorizontalCenter | TextFormatFlags.VerticalCenter);

        TextRenderer.DrawText(g, newsData[i][1], new Font("Segoe UI", 9f * ui, FontStyle.Bold),
            new Point(x + (int)(94 * ui), ry - (int)(2 * ui)), Palette.Title);
        TextRenderer.DrawText(g, newsData[i][2], new Font("Segoe UI", 8f * ui),
            new Point(x + (int)(94 * ui), ry + (int)(15 * ui)), Palette.Dim);
        TextRenderer.DrawText(g, newsData[i][3], new Font("Segoe UI", 7.5f * ui),
            new Point(x + chipRect.Width + (int)(100 * ui), ry + (int)(1 * ui)), Palette.Dim);
    }

    void DrawAddonRow(Graphics g, int nw, int by, int i, int x)
    {
        int ry = by + (int)(48 * ui) + i * (int)(40 * ui);

        var dotRect = new Rectangle(x, ry + (int)(3 * ui), (int)(26 * ui), (int)(16 * ui));
        using (var path = UI.Round(dotRect, (int)(8 * ui)))
        using (var fill = new SolidBrush(Palette.Accent))
            g.FillPath(fill, path);
        TextRenderer.DrawText(g, addonsData[i][1], new Font("Segoe UI", 7.5f * ui, FontStyle.Bold), dotRect, Color.White,
            TextFormatFlags.HorizontalCenter | TextFormatFlags.VerticalCenter);

        TextRenderer.DrawText(g, addonsData[i][0], new Font("Segoe UI", 9.5f * ui),
            new Point(x + (int)(36 * ui), ry), Palette.Text);
    }

    void BuildVersions()
    {
        string[] versions = { "Release 1.21.4", "Snapshot 1.21.4", "Fabric 1.21.4", "Vanilla 1.21.4" };

        pageVersions.Paint += (s, e) =>
        {
            var g = e.Graphics;
            TextRenderer.DrawText(g, "Версии", new Font("Segoe UI", 14f * ui, FontStyle.Bold),
                new Point((int)(4 * ui), (int)(4 * ui)), Palette.Title);

            for (int i = 0; i < versions.Length; i++)
            {
                var rect = new Rectangle((int)(4 * ui), (int)(48 * ui) + i * (int)(58 * ui), pageVersions.Width - (int)(8 * ui), (int)(48 * ui));
                UI.PaintCard(g, rect, (int)(12 * ui), i == 0 ? Palette.Accent : Palette.Surface, i == 0 ? Color.Empty : Color.FromArgb(45, 45, 62));

                TextRenderer.DrawText(g, "Moonlight " + versions[i], new Font("Segoe UI", 10f * ui, FontStyle.Bold),
                    new Point(rect.X + (int)(18 * ui), rect.Y + (int)(14 * ui)), i == 0 ? Color.White : Palette.Text);

                var chipRect = new Rectangle(rect.Right - (int)(100 * ui), rect.Y + (int)(14 * ui), (int)(84 * ui), (int)(20 * ui));
                using (var path = UI.Round(chipRect, (int)(10 * ui)))
                using (var fill = new SolidBrush(i == 0 ? Color.FromArgb(60, Color.White) : Palette.SurfaceLight))
                    g.FillPath(fill, path);
                TextRenderer.DrawText(g, "fabric", new Font("Segoe UI", 7.5f * ui), chipRect, Palette.Text,
                    TextFormatFlags.HorizontalCenter | TextFormatFlags.VerticalCenter);
            }
        };
    }

    void BuildAccount()
    {
        pageAccount.Paint += (s, e) =>
        {
            var g = e.Graphics;
            TextRenderer.DrawText(g, "Аккаунт", new Font("Segoe UI", 14f * ui, FontStyle.Bold),
                new Point((int)(4 * ui), (int)(4 * ui)), Palette.Title);

            var card = new Rectangle((int)(4 * ui), (int)(48 * ui), Math.Min((int)(420 * ui), pageAccount.Width - (int)(8 * ui)), (int)(210 * ui));
            UI.PaintCard(g, card, (int)(16 * ui), Palette.Surface, Color.FromArgb(45, 45, 62));

            TextRenderer.DrawText(g, "Локальный профиль", new Font("Segoe UI", 11f * ui, FontStyle.Bold),
                new Point(card.X + (int)(20 * ui), card.Y + (int)(16 * ui)), Palette.Title);
            TextRenderer.DrawText(g, "Авторизация через Microsoft отключена — играем по нику",
                new Font("Segoe UI", 8f * ui), new Point(card.X + (int)(20 * ui), card.Y + (int)(42 * ui)), Palette.Dim);

            var avatar = new Rectangle(card.X + (int)(20 * ui), card.Y + (int)(70 * ui), (int)(56 * ui), (int)(56 * ui));
            using (var path = UI.Round(avatar, (int)(14 * ui)))
            using (var fill = new SolidBrush(Palette.Accent))
                g.FillPath(fill, path);
            TextRenderer.DrawText(g, "M", new Font("Segoe UI", 18f * ui, FontStyle.Bold), avatar, Color.White,
                TextFormatFlags.HorizontalCenter | TextFormatFlags.VerticalCenter);

            TextRenderer.DrawText(g, "UID 0001", new Font("Segoe UI", 9f * ui),
                new Point(avatar.Right + (int)(16 * ui), avatar.Y + (int)(4 * ui)), Palette.Dim);
            var chip = new Rectangle(avatar.Right + (int)(16 * ui), avatar.Y + (int)(26 * ui), (int)(70 * ui), (int)(20 * ui));
            using (var path = UI.Round(chip, (int)(10 * ui)))
            using (var fill = new SolidBrush(Palette.Success))
                g.FillPath(fill, path);
            TextRenderer.DrawText(g, "Player", new Font("Segoe UI", 7.5f * ui, FontStyle.Bold), chip, Color.White,
                TextFormatFlags.HorizontalCenter | TextFormatFlags.VerticalCenter);
        };

        var nickLabel = UI.TextLabel("НИКНЕЙМ", (int)(26 * ui), (int)(140 * ui), 7.5f, true, Palette.Dim);
        pageAccount.Controls.Add(nickLabel);

        var nickBox = new Panel();
        nickBox.Size = new Size((int)(240 * ui), (int)(42 * ui));
        nickBox.Location = new Point((int)(24 * ui), (int)(160 * ui));
        nickBox.Paint += (s, e) =>
        {
            e.Graphics.SmoothingMode = SmoothingMode.AntiAlias;
            var rect = new Rectangle(0, 0, nickBox.Width - 1, nickBox.Height - 1);
            using (var p = UI.Round(rect, (int)(12 * ui)))
            using (var fill = new SolidBrush(Palette.Text))
            using (var pen = new Pen(accountNick.Focused ? Palette.Accent : Color.FromArgb(50, 50, 68), 1f))
            {
                e.Graphics.FillPath(fill, p);
                e.Graphics.DrawPath(pen, p);
            }
        };
        pageAccount.Controls.Add(nickBox);

        accountNick = new TextBox();
        accountNick.Text = "moonlight";
        accountNick.BorderStyle = BorderStyle.None;
        accountNick.BackColor = Palette.Text;
        accountNick.ForeColor = Color.Black;
        accountNick.Font = new Font("Segoe UI", 11.5f * ui);
        accountNick.Size = new Size(nickBox.Width - (int)(20 * ui), (int)(24 * ui));
        accountNick.Location = new Point((int)(10 * ui), (int)(10 * ui));
        accountNick.TextChanged += (s, e) => SyncNick(accountNick.Text);
        nickBox.Controls.Add(accountNick);
    }

    void BuildWorkshop()
    {
        pageWorkshop.Paint += (s, e) =>
        {
            var g = e.Graphics;
            TextRenderer.DrawText(g, "Мастерская", new Font("Segoe UI", 14f * ui, FontStyle.Bold),
                new Point((int)(4 * ui), (int)(4 * ui)), Palette.Title);
            TextRenderer.DrawText(g, "Все аддоны уже встроены в клиент", new Font("Segoe UI", 8.5f * ui),
                new Point((int)(4 * ui), (int)(34 * ui)), Palette.Dim);

            int cardW = (int)(200 * ui), cardH = (int)(64 * ui);

            for (int i = 0; i < addonsData.Length; i++)
            {
                int col = i % 3, row = i / 3;
                var rect = new Rectangle((int)(4 * ui) + col * (cardW + (int)(12 * ui)),
                    (int)(64 * ui) + row * (cardH + (int)(12 * ui)), cardW, cardH);

                UI.PaintCard(g, rect, (int)(14 * ui), Palette.Surface, Color.FromArgb(45, 45, 62));
                TextRenderer.DrawText(g, addonsData[i][0], new Font("Segoe UI", 10f * ui, FontStyle.Bold),
                    new Point(rect.X + (int)(14 * ui), rect.Y + (int)(12 * ui)), Palette.Title);
                TextRenderer.DrawText(g, "клавиша: " + addonsData[i][1], new Font("Segoe UI", 8f * ui),
                    new Point(rect.X + (int)(14 * ui), rect.Y + (int)(36 * ui)), Palette.Dim);

                var chip = new Rectangle(rect.Right - (int)(92 * ui), rect.Y + (int)(12 * ui), (int)(78 * ui), (int)(20 * ui));
                using (var path = UI.Round(chip, (int)(10 * ui)))
                using (var fill = new SolidBrush(Palette.Success))
                    g.FillPath(fill, path);
                TextRenderer.DrawText(g, "Встроен", new Font("Segoe UI", 7.5f * ui, FontStyle.Bold), chip, Color.White,
                    TextFormatFlags.HorizontalCenter | TextFormatFlags.VerticalCenter);
            }
        };
    }

    void BuildNewsPage()
    {
        pageNews.Paint += (s, e) =>
        {
            var g = e.Graphics;
            TextRenderer.DrawText(g, "Новости", new Font("Segoe UI", 14f * ui, FontStyle.Bold),
                new Point((int)(4 * ui), (int)(4 * ui)), Palette.Title);

            for (int i = 0; i < newsData.Length; i++)
                DrawNewsRow(g, 0, (int)(40 * ui), i, (int)(4 * ui), 0);
        };
    }

    void BuildSettings()
    {
        string[][] rows =
        {
            new[] { "Скрытая консоль", "Игра запускается без чёрного окна" },
            new[] { "Логи в лаунчере", "Кнопка ЛОГ в верхней панели" },
            new[] { "Discord RPC", "game/moonlight/discord-clientid.txt" },
            new[] { "Смена ника", "Главная или Аккаунт, по умолчанию moonlight" },
            new[] { "Язык клиента", "Русский" }
        };

        pageSettings.Paint += (s, e) =>
        {
            var g = e.Graphics;
            TextRenderer.DrawText(g, "Настройки", new Font("Segoe UI", 14f * ui, FontStyle.Bold),
                new Point((int)(4 * ui), (int)(4 * ui)), Palette.Title);

            for (int i = 0; i < rows.Length; i++)
            {
                var rect = new Rectangle((int)(4 * ui), (int)(48 * ui) + i * (int)(56 * ui), pageSettings.Width - (int)(8 * ui), (int)(46 * ui));
                UI.PaintCard(g, rect, (int)(12 * ui), Palette.Surface, Color.FromArgb(45, 45, 62));

                TextRenderer.DrawText(g, rows[i][0], new Font("Segoe UI", 9.5f * ui, FontStyle.Bold),
                    new Point(rect.X + (int)(16 * ui), rect.Y + (int)(8 * ui)), Palette.Title);
                TextRenderer.DrawText(g, rows[i][1], new Font("Segoe UI", 8f * ui),
                    new Point(rect.X + (int)(16 * ui), rect.Y + (int)(26 * ui)), Palette.Dim);
            }
        };
    }

    void ShowPage(int index)
    {
        activeNav = index;
        pageHome.Visible = index == 0;
        pageVersions.Visible = index == 1;
        pageAccount.Visible = index == 2;
        pageWorkshop.Visible = index == 3;
        pageNews.Visible = index == 4;
        pageSettings.Visible = index == 5;
        sidebar.Invalidate();
    }

    void SyncNick(string value)
    {
        if (string.IsNullOrEmpty(value)) value = "moonlight";

        currentNick = value;
        greeting.Text = "С возвращением, " + value + "!";
        sidebar.Invalidate();

        if (heroNick != null && heroNick.Text != value) heroNick.Text = value;
        if (accountNick != null && accountNick.Text != value) accountNick.Text = value;
    }
}
