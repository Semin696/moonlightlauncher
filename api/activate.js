const { OWNER_CODE, getPool, ensureSchema, json, readBody, auth } = require('./_lib');

module.exports = async (req, res) => {
  if (req.method !== 'POST') return json(res, 405, { error: 'Метод не поддерживается' });
  try {
    const user = await auth(req);
    if (!user) return json(res, 401, { error: 'Войдите в аккаунт' });

    const { code } = await readBody(req);
    const value = String(code || '').trim();
    if (!value) return json(res, 400, { error: 'Введите код' });

    await ensureSchema();
    const db = getPool();

    if (value === OWNER_CODE) {
      await db.execute('UPDATE mlv_users SET role = ?, subscribed = 1 WHERE id = ?', ['owner', user.id]);
      return json(res, 200, { ok: true, message: 'Код владельца активирован' });
    }

    const [rows] = await db.execute(
      'SELECT code FROM mlv_codes WHERE code = ? AND used_by IS NULL',
      [value]
    );
    if (rows.length === 0) {
      return json(res, 400, { error: 'Неверный или уже использованный код' });
    }

    await db.execute('UPDATE mlv_users SET subscribed = 1 WHERE id = ?', [user.id]);
    await db.execute('UPDATE mlv_codes SET used_by = ?, used_at = NOW() WHERE code = ?', [
      user.id,
      value,
    ]);
    return json(res, 200, { ok: true, message: 'Подписка активирована' });
  } catch (e) {
    console.error(e);
    return json(res, 500, { error: 'Ошибка сервера' });
  }
};
