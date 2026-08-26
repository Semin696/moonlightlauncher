using System;
using System.Drawing;
using System.Drawing.Drawing2D;
using System.Windows.Forms;

static class Palette
{
    public static Color Background = Color.FromArgb(17, 17, 23);
    public static Color Surface = Color.FromArgb(25, 25, 33);
    public static Color SurfaceLight = Color.FromArgb(34, 34, 44);
    public static Color Accent = Color.FromArgb(106, 72, 227);
    public static Color AccentHover = Color.FromArgb(89, 64, 201);
    public static Color Title = Color.White;
    public static Color Text = Color.FromArgb(223, 223, 243);
    public static Color Dim = Color.FromArgb(125, 125, 150);
    public static Color Success = Color.FromArgb(70, 167, 88);
    public static Color Error = Color.FromArgb(229, 72, 77);
}

static class UI
{
    public static GraphicsPath Round(Rectangle r, int radius)
    {
        var path = new GraphicsPath();
        if (radius < 1) { path.AddRectangle(r); return path; }
        path.AddArc(r.X, r.Y, radius, radius, 180, 90);
        path.AddArc(r.Right - radius, r.Y, radius, radius, 270, 90);
        path.AddArc(r.Right - radius, r.Bottom - radius, radius, radius, 0, 90);
        path.AddArc(r.X, r.Bottom - radius, radius, radius, 90, 90);
        path.CloseFigure();
        return path;
    }

    public static void PaintCard(Graphics g, Rectangle rect, int radius, Color fill, Color border)
    {
        g.SmoothingMode = SmoothingMode.AntiAlias;
        using (var path = Round(rect, radius))
        {
            using (var brush = new SolidBrush(fill))
                g.FillPath(brush, path);
            if (border != Color.Empty)
                using (var pen = new Pen(border, 1f))
                    g.DrawPath(pen, path);
        }
    }

    public static void DrawCrescent(Graphics g, float cx, float cy, float r, Color fill, Color cut)
    {
        g.SmoothingMode = SmoothingMode.AntiAlias;
        using (var brush = new SolidBrush(fill))
            g.FillEllipse(brush, cx - r, cy - r, r * 2, r * 2);
        using (var brush = new SolidBrush(cut))
            g.FillEllipse(brush, cx - r + r * 0.38f, cy - r - r * 0.3f, r * 2, r * 2);
    }

    public static Label TextLabel(string text, int x, int y, float size, bool bold, Color color)
    {
        var label = new Label();
        label.Text = text;
        label.ForeColor = color;
        label.Font = new Font("Segoe UI", size, bold ? FontStyle.Bold : FontStyle.Regular);
        label.AutoSize = true;
        label.BackColor = Color.Transparent;
        label.Location = new Point(x, y);
        return label;
    }
}

class GradientButton : Control
{
    public Color Accent { get; set; }
    public Color AccentDark { get; set; }

    private bool hovered;

    public GradientButton()
    {
        Accent = Palette.Accent;
        AccentDark = Palette.AccentHover;
        SetStyle(ControlStyles.AllPaintingInWmPaint | ControlStyles.OptimizedDoubleBuffer | ControlStyles.UserPaint | ControlStyles.ResizeRedraw, true);
    }

    protected override void OnMouseEnter(EventArgs e) { hovered = true; Invalidate(); base.OnMouseEnter(e); }
    protected override void OnMouseLeave(EventArgs e) { hovered = false; Invalidate(); base.OnMouseLeave(e); }

    protected override void OnPaint(PaintEventArgs e)
    {
        var g = e.Graphics;
        g.SmoothingMode = SmoothingMode.AntiAlias;

        var rect = new Rectangle(0, 0, Width - 1, Height - 1);
        using (var path = UI.Round(rect, Height / 2))
        {
            Color top = hovered ? ControlPaint.Light(Accent, 0.12f) : Accent;
            Color bottom = hovered ? Accent : AccentDark;

            if (!Enabled)
            {
                top = Color.FromArgb(55, 55, 72);
                bottom = Color.FromArgb(45, 45, 60);
            }

            using (var brush = new LinearGradientBrush(rect, top, bottom, 90f))
                g.FillPath(brush, path);

            var sf = new StringFormat { Alignment = StringAlignment.Center, LineAlignment = StringAlignment.Center };
            g.DrawString(Text, Font, Enabled ? Brushes.White : new SolidBrush(Palette.Dim), rect, sf);
        }
    }
}
