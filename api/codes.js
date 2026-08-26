const { getPool, ensureSchema, json, readBody, auth, makeCode } = require('./_lib');

module.exports = async (req, res) => {
  try {
    const user = await auth(req);
    if (!user || user.role !== 'owner') {
      return json(res, 403, { error: 'Доступ только для владельца' });
    }
    await ensureSchema();
    const db = getPool();

    if (req.method === 'GET') {
      const [rows] = await db.execute(
        `SELECT c.code, c.created_at, c.used_at, u.username AS used_by_name
         FROM mlv_codes c LEFT JOIN mlv_users u ON u.id = c.used_by
         ORDER BY c.created_at DESC LIMIT 200`
      );
      return json(res, 200, { ok: true, codes: rows });
    }

    if (req.method === 'POST') {
      const code = makeCode();
      await db.execute('INSERT INTO mlv_codes (code, created_by) VALUES (?, ?)', [code, user.id]);
      return json(res, 200, { ok: true, code });
    }

    if (req.method === 'DELETE') {
      const code = String(req.query.code || '').trim();
      if (!code) return json(res, 400, { error: 'Укажите код' });
      await db.execute('DELETE FROM mlv_codes WHERE code = ? AND used_by IS NULL', [code]);
      return json(res, 200, { ok: true });
    }

    return json(res, 405, { error: 'Метод не поддерживается' });
  } catch (e) {
    console.error(e);
    return json(res, 500, { error: 'Ошибка сервера' });
  }
};
