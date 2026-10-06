// Android runtime compatibility layer. Desktop /api endpoints are intentionally
// not assumed to exist inside the APK. We provide small local fallbacks for UI
// settings and memory data so the shell and character studio never fail just
// because the PC server is absent.
export const isAndroidRuntime = () => typeof window !== 'undefined' && Boolean((window as any).MYRAAAndroid);

const SETTINGS_KEY = 'myraa.android.app-settings.v1';
const MEMORIES_KEY = 'myraa.android.memories.v1';

function defaultSettings() {
  return {
    graphics: { realism: 55, autoStep: true },
    physics: { clothEnabled: true, clothLooseness: 0.5 },
    voice: { pitch: 0 },
    character: { activeCharacterId: 'myraa' },
    developer: { physicsDebug: false },
  };
}

function readSettings() {
  try { return { ...defaultSettings(), ...JSON.parse(localStorage.getItem(SETTINGS_KEY) || '{}') }; }
  catch { return defaultSettings(); }
}
function saveSettings(value: any) {
  const next = { ...readSettings(), ...value };
  try { localStorage.setItem(SETTINGS_KEY, JSON.stringify(next)); } catch {}
  return next;
}
function json(data: unknown, status = 200) {
  return new Response(JSON.stringify(data), { status, headers: { 'Content-Type': 'application/json' } });
}
function readMemories() {
  try { return JSON.parse(localStorage.getItem(MEMORIES_KEY) || '[]'); } catch { return []; }
}
function writeMemories(v: any[]) { try { localStorage.setItem(MEMORIES_KEY, JSON.stringify(v)); } catch {} }

export function installAndroidRuntime() {
  if (!isAndroidRuntime() || (window as any).__MYRAA_ANDROID_RUNTIME__) return;
  (window as any).__MYRAA_ANDROID_RUNTIME__ = true;
  const originalFetch = window.fetch.bind(window);
  window.fetch = async (input: RequestInfo | URL, init?: RequestInit) => {
    const url = typeof input === 'string' ? input : input instanceof URL ? input.toString() : input.url;
    const path = (() => { try { return new URL(url, location.href).pathname; } catch { return url; } })();
    if (!path.startsWith('/api/')) return originalFetch(input, init);

    const method = (init?.method || (typeof input !== 'string' && !(input instanceof URL) ? input.method : 'GET')).toUpperCase();
    let body: any = null;
    try { body = init?.body ? JSON.parse(String(init.body)) : null; } catch {}

    if (path === '/api/config' && method === 'GET') return json({ hasApiKey: Boolean(localStorage.getItem('myraa.android.geminiKey')) });
    if (path === '/api/config/apikey' && method === 'POST') {
      if (!body?.apiKey) return json({ ok: false, error: 'API key is required.' }, 400);
      localStorage.setItem('myraa.android.geminiKey', String(body.apiKey).trim());
      return json({ ok: true });
    }
    if (path === '/api/app-settings' && method === 'GET') return json(readSettings());
    if (path === '/api/app-settings' && method === 'POST') return json(saveSettings(body || {}));
    if (path === '/api/settings' && method === 'GET') return json(readSettings());
    if (path === '/api/settings' && method === 'POST') return json(saveSettings(body || {}));
    if (path === '/api/agent-health') return json({ ok: true, platform: 'android', backend: 'android-local' });
    if (path === '/api/memories' && method === 'GET') return json(readMemories());
    if (path === '/api/memories' && method === 'POST') {
      const memories = readMemories();
      const item = { id: `android-${Date.now()}`, category: body?.category || 'general', text: body?.text || '', createdAt: new Date().toISOString() };
      memories.push(item); writeMemories(memories); return json(item, 201);
    }
    if (path.startsWith('/api/memories/') && method === 'DELETE') {
      const id = decodeURIComponent(path.split('/').pop() || '');
      writeMemories(readMemories().filter((m: any) => m.id !== id)); return json({ success: true });
    }
    if (path === '/api/characters' && method === 'GET') return json([{ id: 'myraa', displayName: 'MYRAA', source: 'built-in', summary: { supported: 1, partial: 0, unsupported: 0, testsPassed: 1, testsFailed: 0 } }]);
    if (path === '/api/characters/myraa' && method === 'GET') return json({ id: 'myraa', displayName: 'MYRAA', source: 'built-in' });
    if (path === '/api/poses' && method === 'GET') return json([]);
    if (path === '/api/poses' && method === 'POST') return json({ id: `android-pose-${Date.now()}`, ...body }, 201);
    if (path.startsWith('/api/poses/') && method === 'DELETE') return json({ ok: true });
    if (path === '/api/characters/import') return json({ error: 'Character import is not available in the Android build yet. Use an Android-supported model file.' }, 501);
    return json({ ok: true, platform: 'android-local' });
  };
}
