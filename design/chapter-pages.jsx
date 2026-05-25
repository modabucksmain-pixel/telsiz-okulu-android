// Per-chapter showcase blocks. Each chapter gets a unique visual identity
// that matches its subject. Rendered above the lesson list in BolumDetay.

const { useState: cState, useEffect: cEffect } = React;

// ── B1 · NATO Alfabesi ─────────────────────────────────────────────────────
function ShowcaseNATO({ t, bolum }) {
  const [hover, setHover] = cState('M');
  const sel = window.NATO.find(r => r[0] === hover) || window.NATO[0];

  return (
    <ShowcaseShell t={t} bolum={bolum}
      eyebrow="A·B·C · 26 HARF + 10 RAKAM"
      hero={
        <div style={{ display: 'flex', alignItems: 'center', gap: 14 }}>
          <div style={{
            width: 78, height: 78, borderRadius: 16,
            background: t.surface, border: `2px solid ${bolum.renk}`,
            display: 'grid', placeItems: 'center',
          }}>
            <span style={{ fontFamily: t.monoFont, fontWeight: 800, fontSize: 38, color: bolum.renk, lineHeight: 1 }}>
              {sel[0]}
            </span>
          </div>
          <div>
            <div style={{ fontFamily: t.monoFont, fontSize: 10.5, letterSpacing: 1.4, color: t.muted, fontWeight: 700 }}>SEÇİLİ</div>
            <div style={{ fontSize: 26, fontWeight: 800, color: t.ink, letterSpacing: -0.5 }}>{sel[1]}</div>
            <div style={{ fontFamily: t.monoFont, fontSize: 12, color: bolum.renk, marginTop: 2, letterSpacing: 1 }}>
              /{sel[2]}/
            </div>
          </div>
        </div>
      }
    >
      <div style={{
        display: 'grid', gridTemplateColumns: 'repeat(9, 1fr)', gap: 4,
        padding: 10, background: t.surface2, borderRadius: 10,
        border: `1px solid ${t.hairline}`,
      }}>
        {window.NATO.map(([l]) => (
          <button key={l}
            onMouseEnter={() => setHover(l)}
            onClick={() => setHover(l)}
            style={{
              aspectRatio: '1 / 1',
              borderRadius: 6,
              border: `1px solid ${hover === l ? bolum.renk : t.hairline2}`,
              background: hover === l ? bolum.renk : t.surface,
              color: hover === l ? '#fff' : t.ink,
              fontFamily: t.monoFont, fontWeight: 700, fontSize: 12,
              cursor: 'pointer', padding: 0,
            }}>
            {l}
          </button>
        ))}
      </div>
      <div style={{ marginTop: 8, fontSize: 11.5, color: t.muted, fontFamily: t.monoFont, textAlign: 'center' }}>
        Bir harfe dokun · telaffuzunu gör
      </div>
    </ShowcaseShell>
  );
}

// ── B2 · Q Kodları ─────────────────────────────────────────────────────────
function ShowcaseQ({ t, bolum }) {
  const featured = ['QRZ', 'QSL', 'QSO', 'QTH', 'QRM', 'QRP'];
  return (
    <ShowcaseShell t={t} bolum={bolum}
      eyebrow="Q-KOD · 15 KISALTMA"
      hero={
        <div>
          <div style={{
            fontFamily: t.monoFont, fontSize: 11, letterSpacing: 1.4,
            color: bolum.renk, fontWeight: 700,
          }}>ÖRNEK QSO</div>
          <div style={{
            marginTop: 8, padding: '12px 14px',
            background: t.surface2, borderRadius: 10,
            border: `1px solid ${t.hairline}`,
            fontFamily: t.monoFont, fontSize: 12.5, color: t.ink, lineHeight: 1.7,
          }}>
            <div>TA1ABC → <b style={{ color: bolum.renk }}>QRZ</b>?</div>
            <div>DL2X →  <b style={{ color: bolum.renk }}>QRZ</b> de DL2X, <b style={{ color: bolum.renk }}>QTH</b> Berlin</div>
            <div>TA1ABC → <b style={{ color: bolum.renk }}>QSL</b>, 73</div>
          </div>
        </div>
      }
    >
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: 8 }}>
        {featured.map(k => {
          const meaning = window.QKODES.find(([code]) => code === k)?.[1] || '';
          return (
            <div key={k} style={{
              padding: '10px 8px', borderRadius: 10,
              background: t.surface, border: `1px solid ${t.hairline}`,
              textAlign: 'center',
            }}>
              <div style={{
                fontFamily: t.monoFont, fontWeight: 800, fontSize: 16, color: bolum.renk, letterSpacing: 1,
              }}>{k}</div>
              <div style={{ fontSize: 10.5, color: t.muted, marginTop: 3, lineHeight: 1.25 }}>{meaning}</div>
            </div>
          );
        })}
      </div>
    </ShowcaseShell>
  );
}

// ── B3 · Elektronik — Ohm üçgeni + formüller ──────────────────────────────
function ShowcaseElektronik({ t, bolum }) {
  return (
    <ShowcaseShell t={t} bolum={bolum}
      eyebrow="OHM · KIRCHHOFF · YARI İLETKEN"
      hero={
        <div style={{ display: 'flex', alignItems: 'center', gap: 18 }}>
          <OhmTriangle t={t} accent={bolum.renk} />
          <div>
            <div style={{ fontFamily: t.monoFont, fontSize: 10.5, letterSpacing: 1.4, color: t.muted, fontWeight: 700 }}>OHM ÜÇGENİ</div>
            <div style={{ fontFamily: t.monoFont, fontSize: 22, fontWeight: 800, color: t.ink, marginTop: 4 }}>
              V = I · R
            </div>
            <div style={{ fontFamily: t.monoFont, fontSize: 13, color: t.muted, marginTop: 2 }}>
              I = V / R<br/>R = V / I
            </div>
          </div>
        </div>
      }
    >
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 8 }}>
        {[
          ['VOLT', 'V', 'Gerilim'],
          ['AMPER', 'A', 'Akım'],
          ['OHM', 'Ω', 'Direnç'],
          ['WATT', 'W', 'Güç'],
        ].map(([name, sym, desc]) => (
          <div key={name} style={{
            display: 'flex', alignItems: 'center', gap: 10,
            padding: '10px 12px', borderRadius: 10,
            background: t.surface, border: `1px solid ${t.hairline}`,
          }}>
            <div style={{
              width: 32, height: 32, borderRadius: 8,
              background: bolum.renk + '15', color: bolum.renk,
              display: 'grid', placeItems: 'center',
              fontFamily: t.monoFont, fontWeight: 800, fontSize: 16,
            }}>{sym}</div>
            <div>
              <div style={{ fontFamily: t.monoFont, fontWeight: 700, fontSize: 11, color: t.ink, letterSpacing: 0.5 }}>{name}</div>
              <div style={{ fontSize: 11, color: t.muted }}>{desc}</div>
            </div>
          </div>
        ))}
      </div>
    </ShowcaseShell>
  );
}

function OhmTriangle({ t, accent }) {
  // V on top, I·R on bottom
  return (
    <div style={{ position: 'relative', width: 96, height: 90 }}>
      <svg viewBox="0 0 100 90" width="96" height="90">
        <path d="M50 6 L94 82 L6 82 Z" fill={accent + '12'} stroke={accent} strokeWidth="2" strokeLinejoin="round"/>
        <line x1="6" y1="56" x2="94" y2="56" stroke={accent} strokeWidth="1.5" opacity="0.4"/>
        <line x1="50" y1="56" x2="50" y2="82" stroke={accent} strokeWidth="1.5" opacity="0.4"/>
      </svg>
      <div style={{
        position: 'absolute', left: 0, right: 0, top: 14, textAlign: 'center',
        fontFamily: t.monoFont, fontWeight: 800, fontSize: 22, color: accent,
      }}>V</div>
      <div style={{
        position: 'absolute', left: 0, top: 56, width: '50%', textAlign: 'center',
        fontFamily: t.monoFont, fontWeight: 800, fontSize: 18, color: accent,
        lineHeight: '26px',
      }}>I</div>
      <div style={{
        position: 'absolute', right: 0, top: 56, width: '50%', textAlign: 'center',
        fontFamily: t.monoFont, fontWeight: 800, fontSize: 18, color: accent,
        lineHeight: '26px',
      }}>R</div>
    </div>
  );
}

// ── B4 · Frekans ve Bantlar — spektrum görselleştirme ─────────────────────
function ShowcaseFrekans({ t, bolum }) {
  const bands = [
    { name: '80m', mhz: '3.5', col: '#0369A1' },
    { name: '40m', mhz: '7.0', col: '#0891B2' },
    { name: '20m', mhz: '14',  col: '#0F766E' },
    { name: '15m', mhz: '21',  col: '#15803D' },
    { name: '10m', mhz: '28',  col: '#65A30D' },
    { name: '2m',  mhz: '144', col: '#CA8A04' },
    { name: '70cm',mhz: '435', col: '#B45309' },
  ];
  const [sel, setSel] = cState(2);

  return (
    <ShowcaseShell t={t} bolum={bolum}
      eyebrow="HF · VHF · UHF"
      hero={
        <div>
          <div style={{ display: 'flex', alignItems: 'baseline', gap: 8 }}>
            <span style={{ fontFamily: t.monoFont, fontWeight: 800, fontSize: 36, color: t.ink, letterSpacing: -1 }}>
              {bands[sel].mhz}
            </span>
            <span style={{ fontFamily: t.monoFont, fontSize: 14, color: t.muted, fontWeight: 600 }}>MHz</span>
            <span style={{
              marginLeft: 'auto',
              fontFamily: t.monoFont, fontWeight: 700, fontSize: 11,
              padding: '4px 8px', borderRadius: 6,
              background: bands[sel].col + '15', color: bands[sel].col,
              letterSpacing: 1,
            }}>BAND {bands[sel].name}</span>
          </div>
          <SpectrumChart t={t} bands={bands} sel={sel} />
        </div>
      }
    >
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(7, 1fr)', gap: 4 }}>
        {bands.map((b, i) => (
          <button key={b.name} onClick={() => setSel(i)} style={{
            padding: '8px 4px', borderRadius: 6,
            background: sel === i ? b.col : t.surface,
            color: sel === i ? '#fff' : t.ink,
            border: `1px solid ${sel === i ? b.col : t.hairline}`,
            fontFamily: t.monoFont, fontWeight: 700, fontSize: 10.5,
            cursor: 'pointer', textAlign: 'center',
          }}>{b.name}</button>
        ))}
      </div>
    </ShowcaseShell>
  );
}

function SpectrumChart({ t, bands, sel }) {
  return (
    <div style={{
      marginTop: 10, padding: 10, borderRadius: 8,
      background: t.surface2, border: `1px solid ${t.hairline}`,
      position: 'relative', height: 72,
    }}>
      <svg width="100%" height="56" preserveAspectRatio="none" style={{ display: 'block' }}>
        {/* baseline */}
        <line x1="0" y1="48" x2="100%" y2="48" stroke={t.hairline2} strokeWidth="1"/>
        {/* band bars */}
        {bands.map((b, i) => {
          const x = (i / bands.length) * 100;
          const w = (1 / bands.length) * 100 - 1;
          const h = 18 + Math.abs(Math.sin(i * 0.7) * 22);
          return (
            <rect key={b.name}
              x={`${x}%`} y={48 - h} width={`${w}%`} height={h}
              fill={b.col} opacity={i === sel ? 1 : 0.3}
              rx="1.5"
            />
          );
        })}
      </svg>
      <div style={{
        position: 'absolute', bottom: 6, left: 10, right: 10,
        display: 'flex', justifyContent: 'space-between',
        fontFamily: t.monoFont, fontSize: 9, color: t.muted, letterSpacing: 0.5,
      }}>
        <span>1.8</span><span>14</span><span>50</span><span>144</span><span>435 MHz</span>
      </div>
    </div>
  );
}

// ── B5 · Prosedürler — sample QSO transcript ──────────────────────────────
function ShowcaseProsedur({ t, bolum }) {
  return (
    <ShowcaseShell t={t} bolum={bolum}
      eyebrow="QSO · ÇAĞRI · ACİL"
      hero={
        <div>
          <div style={{
            fontFamily: t.monoFont, fontSize: 11, letterSpacing: 1.4, color: bolum.renk, fontWeight: 700,
          }}>CQ ÇAĞRISI</div>
          <div style={{
            marginTop: 8, padding: '12px 14px',
            background: t.surface2, borderRadius: 10,
            border: `1px solid ${t.hairline}`,
            fontFamily: t.monoFont, fontSize: 12, color: t.ink, lineHeight: 1.7,
          }}>
            <div>"<b>CQ CQ CQ</b>, this is</div>
            <div>TANGO ALPHA ONE</div>
            <div>ALPHA BRAVO CHARLIE,</div>
            <div>standing by, <b>OVER</b>."</div>
          </div>
        </div>
      }
    >
      <div style={{ display: 'grid', gap: 6 }}>
        {[
          ['OVER',   'Ben konuştum, sıra sende'],
          ['ROGER',  'Anlaşıldı'],
          ['WILCO',  'Anlaşıldı, uygulayacağım'],
          ['MAYDAY', 'Hayati tehlike — acil yardım', true],
          ['PAN-PAN','Acil ama hayati değil', true],
        ].map(([k, v, urgent]) => (
          <div key={k} style={{
            display: 'flex', alignItems: 'center', gap: 10,
            padding: '8px 12px', borderRadius: 8,
            background: t.surface,
            border: `1px solid ${urgent ? t.danger + '50' : t.hairline}`,
          }}>
            <div style={{
              fontFamily: t.monoFont, fontWeight: 800, fontSize: 12.5,
              color: urgent ? t.danger : bolum.renk,
              letterSpacing: 1, minWidth: 72,
            }}>{k}</div>
            <div style={{ fontSize: 12, color: t.ink2, flex: 1 }}>{v}</div>
          </div>
        ))}
      </div>
    </ShowcaseShell>
  );
}

// ── B6 · TRAC Sınav Hazırlık ───────────────────────────────────────────────
function ShowcaseTRAC({ t, bolum, state }) {
  return (
    <ShowcaseShell t={t} bolum={bolum}
      eyebrow={`T-${String(state.examDays).padStart(2,'0')} GÜN · TRAC`}
      hero={
        <div>
          <div style={{ display: 'flex', alignItems: 'baseline', justifyContent: 'space-between' }}>
            <div>
              <div style={{ fontFamily: t.monoFont, fontSize: 10.5, letterSpacing: 1.4, color: t.muted, fontWeight: 700 }}>HAZIRLIK</div>
              <div style={{
                fontFamily: t.monoFont, fontWeight: 800, fontSize: 36, color: t.ink, letterSpacing: -1,
              }}>{Math.round(state.examReady * 100)}<span style={{ fontSize: 18, color: t.muted }}>%</span></div>
            </div>
            <div style={{
              padding: '8px 12px', borderRadius: 8,
              background: bolum.renk + '15', border: `1px solid ${bolum.renk}40`,
            }}>
              <div style={{ fontFamily: t.monoFont, fontSize: 9.5, letterSpacing: 1.2, color: bolum.renk, fontWeight: 700 }}>SINAV</div>
              <div style={{ fontFamily: t.monoFont, fontWeight: 800, fontSize: 18, color: bolum.renk, letterSpacing: -0.5 }}>
                {state.examDays} GÜN
              </div>
            </div>
          </div>
          <window.Bar t={t} pct={state.examReady} color={bolum.renk} style={{ marginTop: 12 }} height={8}/>
        </div>
      }
    >
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: 8 }}>
        <MiniStat t={t} label="DENEME" value={state.denemeSinav} />
        <MiniStat t={t} label="ORT.PUAN" value={`${state.ortalamaPuan}%`} hilite={t.success}/>
        <MiniStat t={t} label="ZAYIF" value={state.zayifKonu} hilite={t.streak}/>
      </div>
      <div style={{
        marginTop: 10, padding: '10px 12px', borderRadius: 8,
        background: t.surface2, border: `1px dashed ${t.hairline2}`,
        fontSize: 12, color: t.ink2, lineHeight: 1.5,
      }}>
        <span style={{ fontFamily: t.monoFont, fontWeight: 700, color: bolum.renk, letterSpacing: 0.5 }}>İPUCU · </span>
        Sınav öncesi son 2 hafta günde 1 tam deneme + zayıf konu tekrarı yap.
      </div>
    </ShowcaseShell>
  );
}

function MiniStat({ t, label, value, hilite }) {
  return (
    <div style={{
      padding: 10, borderRadius: 8,
      background: t.surface, border: `1px solid ${t.hairline}`,
      textAlign: 'center',
    }}>
      <div style={{ fontFamily: t.monoFont, fontSize: 9, letterSpacing: 1.2, color: t.muted, fontWeight: 700 }}>{label}</div>
      <div style={{ fontFamily: t.monoFont, fontWeight: 800, fontSize: 18, color: hilite || t.ink, marginTop: 2 }}>{value}</div>
    </div>
  );
}

// ── Shared shell — eyebrow + hero block + slot ─────────────────────────────
function ShowcaseShell({ t, bolum, eyebrow, hero, children }) {
  return (
    <div style={{ marginBottom: 14 }}>
      <div style={{
        background: t.surface, borderRadius: t.radius,
        border: `1px solid ${t.hairline}`,
        padding: 16, position: 'relative', overflow: 'hidden',
      }}>
        <window.SpectrumBg t={t} accent={bolum.renk} />
        <div style={{ position: 'relative' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline', marginBottom: 12 }}>
            <div style={{
              fontFamily: t.monoFont, fontSize: 10.5, letterSpacing: 1.6,
              color: bolum.renk, fontWeight: 700,
            }}>▸ {eyebrow}</div>
            <div style={{ fontFamily: t.monoFont, fontSize: 10, color: t.muted }}>
              BÖLÜM {String(bolum.no).padStart(2,'0')} / 06
            </div>
          </div>
          {hero}
          <div style={{ marginTop: 14 }}>
            {children}
          </div>
        </div>
      </div>
    </div>
  );
}

// Router for showcase
window.ChapterShowcase = function ({ t, bolum, state }) {
  switch (bolum.id) {
    case 'b1': return <ShowcaseNATO       t={t} bolum={bolum} />;
    case 'b2': return <ShowcaseQ          t={t} bolum={bolum} />;
    case 'b3': return <ShowcaseElektronik t={t} bolum={bolum} />;
    case 'b4': return <ShowcaseFrekans    t={t} bolum={bolum} />;
    case 'b5': return <ShowcaseProsedur   t={t} bolum={bolum} />;
    case 'b6': return <ShowcaseTRAC       t={t} bolum={bolum} state={state} />;
    default:   return null;
  }
};
