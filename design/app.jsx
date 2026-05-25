// App shell: state, router, bottom nav, tweaks panel
const { useState: appState, useReducer: appReducer, useEffect: appEffect, useMemo: appMemo } = React;

const DEFAULTS = /*EDITMODE-BEGIN*/{
  "variant": "spektrum",
  "accent": "blue",
  "progress": "orta",
  "density": "rahat",
  "iconStyle": "monoglyph",
  "showSpectrum": true,
  "callsign": "TA2/YOUR"
}/*EDITMODE-END*/;

function App() {
  const [tweaks, setTweak] = window.useTweaks(DEFAULTS);
  const t = appMemo(() => window.makeTheme(tweaks.variant, tweaks.accent), [tweaks.variant, tweaks.accent]);

  const [nav, setNav] = appState({ screen: 'home' });

  // Build state based on progress level
  const initial = appMemo(() => makeInitialState(tweaks.progress), [tweaks.progress]);
  const [state, dispatch] = appReducer(reducer, initial);

  // Resync when progress level changes
  appEffect(() => { dispatch({ type: 'reset', state: initial }); }, [initial]);

  const go = (next) => setNav(next);

  let screen;
  switch (nav.screen) {
    case 'home':    screen = <window.Home t={t} state={state} dispatch={dispatch} go={go} />; break;
    case 'bolum':   screen = <window.BolumDetay t={t} state={state} go={go} bolumId={nav.bolumId} />; break;
    case 'lesson':  screen = <window.Ders t={t} state={state} dispatch={dispatch} go={go} bolumId={nav.bolumId} dersId={nav.dersId} />; break;
    case 'sinav':   screen = <window.Sinav t={t} state={state} go={go} />; break;
    case 'kutuphane': screen = <window.Kutuphane t={t} go={go} />; break;
    case 'profil':  screen = <window.Profil t={t} state={state} go={go} />; break;
    case 'pratik':  screen = <window.Pratik t={t} state={state} go={go} />; break;
    default: screen = <div>404</div>;
  }

  // Hide bottom nav in lesson screen
  const showNav = nav.screen !== 'lesson';

  return (
    <div className="app-shell" style={{
      width: '100%', maxWidth: 440, height: '100%', margin: '0 auto',
      background: t.bg, color: t.ink,
      fontFamily: t.sansFont,
      display: 'flex', flexDirection: 'column',
      position: 'relative', overflow: 'hidden',
      borderLeft: `1px solid ${t.hairline}`,
      borderRight: `1px solid ${t.hairline}`,
    }}>
      <div className="scroll-area" style={{ flex: 1, overflowY: 'auto', overflowX: 'hidden' }}>
        {screen}
      </div>
      {showNav && <BottomNav t={t} nav={nav} go={go} />}
      <window.TweaksPanel title="Tweaks">
        <window.TweakSection label="Varyasyon">
          <window.TweakRadio
            label="Tema" value={tweaks.variant}
            options={[
              { value: 'sade',     label: 'Sade' },
              { value: 'saha',     label: 'Saha' },
              { value: 'spektrum', label: 'Spektrum' },
            ]}
            onChange={(v) => setTweak('variant', v)}
          />
          <window.TweakRadio
            label="Aksent" value={tweaks.accent}
            options={[
              { value: 'blue',  label: 'Mavi' },
              { value: 'teal',  label: 'Yeşil' },
              { value: 'amber', label: 'Amber' },
            ]}
            onChange={(v) => setTweak('accent', v)}
          />
        </window.TweakSection>
        <window.TweakSection label="Durum">
          <window.TweakRadio
            label="İlerleme" value={tweaks.progress}
            options={[
              { value: 'yeni',  label: 'Yeni' },
              { value: 'orta',  label: 'Orta' },
              { value: 'sinav', label: 'Sınav' },
            ]}
            onChange={(v) => setTweak('progress', v)}
          />
          <window.TweakText label="Çağrı işareti" value={tweaks.callsign}
            onChange={(v) => setTweak('callsign', v)} />
        </window.TweakSection>
        <window.TweakSection label="Hızlı git">
          <window.TweakButton label="Ana Sayfa"   onClick={() => go({ screen: 'home' })} />
          <window.TweakButton label="Bölüm Detay" onClick={() => go({ screen: 'bolum', bolumId: 'b1' })} />
          <window.TweakButton label="Ders"        onClick={() => go({ screen: 'lesson', bolumId: 'b1', dersId: 'b1l1' })} />
          <window.TweakButton label="Sınav"       onClick={() => go({ screen: 'sinav' })} />
          <window.TweakButton label="Kütüphane"   onClick={() => go({ screen: 'kutuphane' })} />
          <window.TweakButton label="Profil"      onClick={() => go({ screen: 'profil' })} />
        </window.TweakSection>
      </window.TweaksPanel>
    </div>
  );
}

// ──────────────────────────────────────────────────────────────
// State
// ──────────────────────────────────────────────────────────────
function makeInitialState(progress) {
  // build per-lesson status map
  const ilerleme = {};
  let xp = 0, streak = 0, tamamlananDers = 0, examDays = 90, examReady = 0.15;
  let denemeSinav = 0, ortalamaPuan = 0, zayifKonu = 0;
  const cur = window.CURRICULUM;

  if (progress === 'yeni') {
    // First lesson barely started
    cur[0].dersler.forEach((l, i) => { ilerleme[l.id] = i === 0 ? 'current' : 'locked'; });
    xp = 35; streak = 1; tamamlananDers = 0;
    examDays = 120; examReady = 0.05; denemeSinav = 0; ortalamaPuan = 0; zayifKonu = 1;
  } else if (progress === 'orta') {
    // first 2 chapters done, mid 3rd
    cur.forEach((b, bi) => {
      b.dersler.forEach((l, li) => {
        if (bi < 2) ilerleme[l.id] = 'done';
        else if (bi === 2 && li < 2) ilerleme[l.id] = 'done';
        else if (bi === 2 && li === 2) ilerleme[l.id] = 'current';
        else ilerleme[l.id] = 'locked';
      });
    });
    xp = 1240; streak = 12; tamamlananDers = 12;
    examDays = 38; examReady = 0.42; denemeSinav = 3; ortalamaPuan = 68; zayifKonu = 5;
  } else { // sinav
    cur.forEach((b, bi) => {
      b.dersler.forEach((l, li) => {
        if (bi < 5) ilerleme[l.id] = 'done';
        else if (li < 3) ilerleme[l.id] = 'done';
        else if (li === 3) ilerleme[l.id] = 'current';
        else ilerleme[l.id] = 'locked';
      });
    });
    xp = 3850; streak = 47; tamamlananDers = 28;
    examDays = 9; examReady = 0.86; denemeSinav = 14; ortalamaPuan = 84; zayifKonu = 3;
  }
  return { ilerleme, xp, streak, tamamlananDers, examDays, examReady, denemeSinav, ortalamaPuan, zayifKonu };
}

function reducer(state, action) {
  switch (action.type) {
    case 'reset': return action.state;
    case 'complete-lesson': {
      const ilerleme = { ...state.ilerleme, [action.dersId]: 'done' };
      // unlock next
      const cur = window.CURRICULUM;
      let nextId = null;
      outer: for (const b of cur) {
        for (let i = 0; i < b.dersler.length; i++) {
          if (b.dersler[i].id === action.dersId && i + 1 < b.dersler.length) {
            nextId = b.dersler[i + 1].id;
            break outer;
          }
        }
      }
      if (nextId && (ilerleme[nextId] === 'locked' || !ilerleme[nextId])) {
        ilerleme[nextId] = 'current';
      }
      return {
        ...state, ilerleme,
        xp: state.xp + (action.xp || 20),
        tamamlananDers: state.tamamlananDers + 1,
      };
    }
    default: return state;
  }
}

// ──────────────────────────────────────────────────────────────
// Bottom navigation
// ──────────────────────────────────────────────────────────────
function BottomNav({ t, nav, go }) {
  const items = [
    { id: 'home',      label: 'Ana Sayfa', icon: HomeIcon, target: { screen: 'home' } },
    { id: 'kutuphane', label: 'Dersler',   icon: BookIcon, target: { screen: 'kutuphane' } },
    { id: 'sinav',     label: 'Sınav',     icon: ExamIcon, target: { screen: 'sinav' } },
    { id: 'profil',    label: 'Profil',    icon: UserIcon, target: { screen: 'profil' } },
  ];
  // map current screen to nav id
  const active = nav.screen === 'home' ? 'home'
    : nav.screen === 'kutuphane' ? 'kutuphane'
    : nav.screen === 'sinav' ? 'sinav'
    : nav.screen === 'profil' ? 'profil'
    : nav.screen === 'bolum' ? 'home'
    : nav.screen === 'pratik' ? 'home'
    : null;

  return (
    <div style={{
      flexShrink: 0,
      background: t.surface,
      borderTop: `1px solid ${t.hairline}`,
      display: 'grid', gridTemplateColumns: '1fr 1fr 1fr 1fr',
      paddingBottom: 6,
    }}>
      {items.map(it => {
        const isActive = active === it.id;
        return (
          <button key={it.id} onClick={() => go(it.target)} style={{
            background: 'transparent', border: 'none',
            display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 2,
            padding: '10px 0 6px', cursor: 'pointer', position: 'relative',
          }}>
            <it.icon active={isActive} color={isActive ? t.accent : t.muted} />
            <span style={{
              fontSize: 10, fontFamily: t.monoFont, fontWeight: 700, letterSpacing: 0.5,
              color: isActive ? t.accent : t.muted,
            }}>{it.label.toUpperCase()}</span>
            {isActive && (
              <span style={{
                position: 'absolute', top: 0, left: '30%', right: '30%', height: 2,
                background: t.accent, borderRadius: 1,
              }}/>
            )}
          </button>
        );
      })}
    </div>
  );
}

function HomeIcon({ active, color }) {
  return (
    <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
      <path d="M3 9l7-6 7 6v8a1 1 0 01-1 1h-3v-5H7v5H4a1 1 0 01-1-1V9z"
        stroke={color} strokeWidth="1.5" fill={active ? color : 'none'} fillOpacity={active ? 0.12 : 0} strokeLinejoin="round"/>
    </svg>
  );
}
function BookIcon({ active, color }) {
  return (
    <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
      <path d="M3 4h6a2 2 0 012 2v10a2 2 0 00-2-2H3V4zM17 4h-6a2 2 0 00-2 2v10a2 2 0 012-2h6V4z"
        stroke={color} strokeWidth="1.5" fill={active ? color : 'none'} fillOpacity={active ? 0.12 : 0} strokeLinejoin="round"/>
    </svg>
  );
}
function ExamIcon({ active, color }) {
  return (
    <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
      <rect x="4" y="3" width="12" height="14" rx="1.5" stroke={color} strokeWidth="1.5"
        fill={active ? color : 'none'} fillOpacity={active ? 0.12 : 0}/>
      <path d="M7 7h6M7 10h6M7 13h4" stroke={color} strokeWidth="1.5" strokeLinecap="round"/>
    </svg>
  );
}
function UserIcon({ active, color }) {
  return (
    <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
      <circle cx="10" cy="7" r="3" stroke={color} strokeWidth="1.5"
        fill={active ? color : 'none'} fillOpacity={active ? 0.12 : 0}/>
      <path d="M3.5 17c1-3.5 4-5 6.5-5s5.5 1.5 6.5 5" stroke={color} strokeWidth="1.5" strokeLinecap="round"/>
    </svg>
  );
}

// Mount
const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(<App />);
