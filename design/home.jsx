// Home screen — v2 redesign
// Consolidates topbar + stats + chapter selector + continue CTA into one
// "operator panel" hero. Lesson list gets a connected vertical track.
const { useState: hState, useMemo: hMemo, useEffect: hEffect, useRef: hRef } = React;

// ──────────────────────────────────────────────────────────────
// Home
// ──────────────────────────────────────────────────────────────
function Home({ t, state, dispatch, go }) {
  const cur = window.CURRICULUM;

  const activeChIdx = hMemo(() => {
    for (let i = 0; i < cur.length; i++) {
      if (window.chapterProgress(cur[i], state.ilerleme).done < cur[i].dersler.length) return i;
    }
    return cur.length - 1;
  }, [state.ilerleme]);

  const [selIdx, setSelIdx] = hState(activeChIdx);
  const [flashColor, setFlashColor] = hState(null);
  const [toast, setToast] = hState(null);
  const [grillOpen, setGrillOpen] = hState(false);
  const [mood, setMood] = hState('idle');
  const [speech, setSpeech] = hState(null);
  const [ripple, setRipple] = hState(null);
  const [ding, setDing] = hState(null);

  hEffect(() => { setSelIdx(activeChIdx); }, [activeChIdx]);

  // Greeter — rotating speech every 7s
  hEffect(() => {
    const tips = [
      'Selam ' + (state.callsign || 'operatör') + '!',
      'Bugün bir ders alalım mı?',
      'Antenler ayakta · QRV',
      'TRANSMIT\'a bas, başlayalım ⚡',
      'NATO alfabesi: A → Alpha',
      'CQ CQ DE TA2/Sen',
      bolum && `${bolum.kod} bandındayız`,
    ].filter(Boolean);
    let i = Math.floor(Math.random() * tips.length);
    setSpeech(tips[i]);
    const id = setInterval(() => {
      i = (i + 1) % tips.length;
      setSpeech(tips[i]);
    }, 7000);
    return () => clearInterval(id);
  }, [selIdx]);

  // Helper to flash a transient speech bubble
  const say = (msg, ms = 2400) => {
    setSpeech(msg);
    clearTimeout(window._speechTimer);
    window._speechTimer = setTimeout(() => setSpeech(null), ms);
  };

  const flashMood = (m, ms = 600) => {
    setMood(m);
    clearTimeout(window._moodTimer);
    window._moodTimer = setTimeout(() => setMood('idle'), ms);
  };

  const isChapterLocked = (idx) => {
    if (idx === 0) return false;
    return window.chapterProgress(cur[idx - 1], state.ilerleme).done < cur[idx - 1].dersler.length;
  };

  const selectChapter = (idx) => {
    if (idx < 0 || idx >= cur.length) return;
    if (isChapterLocked(idx)) {
      setToast(`Önce "${cur[idx - 1].baslik}" bölümünü tamamla`);
      clearTimeout(window._toastTimer);
      window._toastTimer = setTimeout(() => setToast(null), 2800);
      say('Hâlâ kilitli ❗', 1800);
      flashMood('think', 900);
      return;
    }
    if (idx !== selIdx) {
      setFlashColor(cur[idx].renk);
      setTimeout(() => setFlashColor(null), 400);
      flashMood('tuning', 500);
      say(`${cur[idx].kod} bandına geçtim`, 1800);
    }
    setSelIdx(idx);
  };

  const onTransmit = () => {
    if (!nextLesson) return;
    flashMood('transmit', 700);
    setRipple(Date.now());
    setTimeout(() => {
      go({ screen: 'lesson', bolumId: bolum.id, dersId: nextLesson.id });
    }, 380);
  };

  const bolum = cur[selIdx];
  const nextLesson = hMemo(() => {
    for (const l of bolum.dersler) {
      if ((state.ilerleme[l.id] || 'locked') !== 'done') return l;
    }
    return null;
  }, [selIdx, state.ilerleme]);

  const lvl = level(state.xp);
  const prog = window.chapterProgress(bolum, state.ilerleme);

  return (
    <div className="screen home" style={{ background: t.bg, position: 'relative', minHeight: '100%' }}>

      {/* Flash overlay */}
      <div style={{
        position: 'fixed', inset: 0, pointerEvents: 'none', zIndex: 100,
        background: flashColor || 'transparent',
        opacity: flashColor ? 0.09 : 0,
        transition: flashColor ? 'opacity 0s' : 'opacity 0.4s ease',
      }} />

      <OperatorPanel
        t={t} cur={cur} bolum={bolum} selIdx={selIdx}
        state={state} lvl={lvl} prog={prog} nextLesson={nextLesson}
        onSelect={selectChapter} isLocked={isChapterLocked}
        onTransmit={onTransmit}
        mood={mood} speech={speech} ripple={ripple}
        onHoverTransmit={() => { flashMood('wink', 800); }}
      />

      <ExamStrip t={t} state={state} go={go} />

      <div style={{ padding: '0 16px' }}>
        {window.ChapterShowcase && (
          <window.ChapterShowcase t={t} bolum={bolum} state={state} />
        )}

        <SectionHeader t={t} bolum={bolum} prog={prog} />

        <LessonTrack
          t={t} bolum={bolum} state={state}
          isChapterLocked={isChapterLocked(selIdx)}
          onClick={(d) => go({ screen: 'lesson', bolumId: bolum.id, dersId: d.id })}
        />

        <DailyMorse t={t} />
        <div style={{ height: 100 }} />
      </div>

      <GrillMeButton t={t} onClick={() => setGrillOpen(true)} />
      {grillOpen && <GrillMeModal t={t} bolum={bolum} onClose={() => setGrillOpen(false)} />}
      {toast && <Toast t={t} message={toast} />}
    </div>
  );
}

// ──────────────────────────────────────────────────────────────
// Operator panel — single hero combining identity, dial, CTA
// ──────────────────────────────────────────────────────────────
function OperatorPanel({ t, cur, bolum, selIdx, state, lvl, prog, nextLesson, onSelect, isLocked, onTransmit, mood, speech, ripple, onHoverTransmit }) {
  return (
    <div style={{ padding: '14px 14px 0' }}>
      <div style={{
        position: 'relative', overflow: 'hidden',
        background: t.surface, border: `1px solid ${t.hairline}`,
        borderRadius: t.radius + 2,
        boxShadow: `0 1px 0 ${t.hairline2}40, inset 0 1px 0 ${t.hairline}`,
        transition: 'border-color 0.3s',
      }}>
        {/* atmospheric spectrum behind everything */}
        <PanelBg t={t} accent={bolum.renk} animated />

        <div style={{ position: 'relative', padding: '12px 14px 14px' }}>
          {/* row 1: identity + chips */}
          <IdentityRow t={t} lvl={lvl} state={state} mood={mood} speech={speech} accent={bolum.renk} />

          {/* divider */}
          <div style={{
            height: 1, background: t.hairline,
            margin: '12px -14px 12px',
          }} />

          {/* row 2: frequency dial */}
          <FrequencyDial
            t={t} cur={cur} selIdx={selIdx}
            state={state} onSelect={onSelect} isLocked={isLocked}
          />

          {/* row 3: chapter title + progress */}
          <div style={{ marginTop: 12, display: 'flex', alignItems: 'flex-end', justifyContent: 'space-between', gap: 12 }}>
            <div style={{ minWidth: 0 }}>
              <div style={{
                fontFamily: t.monoFont, fontSize: 10, letterSpacing: 1.6,
                color: bolum.renk, fontWeight: 700,
              }}>
                BÖLÜM {String(bolum.no).padStart(2, '0')} · {bolum.kod}
              </div>
              <div style={{
                fontSize: 20, fontWeight: 800, color: t.ink,
                letterSpacing: -0.4, marginTop: 2, lineHeight: 1.1,
              }}>{bolum.baslik}</div>
            </div>
            <div style={{ flexShrink: 0, textAlign: 'right' }}>
              <div style={{
                fontFamily: t.monoFont, fontWeight: 800, fontSize: 16,
                color: t.ink, letterSpacing: -0.5,
              }}>
                {prog.done}<span style={{ color: t.muted, fontWeight: 600 }}>/{prog.total}</span>
              </div>
              <div style={{ fontFamily: t.monoFont, fontSize: 9.5, color: t.muted, letterSpacing: 1, marginTop: -1 }}>
                DERS
              </div>
            </div>
          </div>

          {/* segmented progress */}
          <SegmentedProgress t={t} bolum={bolum} state={state} style={{ marginTop: 10 }} />

          {/* row 4: TRANSMIT CTA */}
          {nextLesson && (
            <TransmitButton t={t} bolum={bolum} ders={nextLesson} onClick={onTransmit}
              onHover={onHoverTransmit} ripple={ripple} />
          )}
        </div>
      </div>
    </div>
  );
}

function PanelBg({ t, accent, animated = false }) {
  return (
    <div style={{
      position: 'absolute', inset: 0, pointerEvents: 'none',
      background: `radial-gradient(120% 80% at 100% 0%, ${accent}18, transparent 55%),
                   radial-gradient(80% 60% at 0% 100%, ${accent}10, transparent 55%)`,
    }}>
      <svg width="100%" height="100%" preserveAspectRatio="none" style={{ position: 'absolute', inset: 0, opacity: 0.5 }}>
        {Array.from({ length: 42 }).map((_, i) => {
          const x = (i / 42) * 100;
          const h = 4 + Math.abs(Math.sin(i * 0.7) * 16) + (i % 8 === 0 ? 14 : 0);
          return (
            <rect key={i} x={`${x}%`} y={0} width="0.9" height={h}
              fill={accent} opacity={0.18 + (i % 4) * 0.05}
              style={animated ? {
                transformBox: 'fill-box', transformOrigin: 'center top',
                animation: `spectrum-wave ${2 + (i % 5) * 0.4}s ease-in-out infinite`,
                animationDelay: `${i * 0.07}s`,
              } : undefined}
            />
          );
        })}
      </svg>
    </div>
  );
}

// Identity row: mascot + speech bubble + stat chips
function IdentityRow({ t, lvl, state, mood, speech, accent }) {
  return (
    <div style={{ display: 'flex', alignItems: 'flex-start', gap: 12, minHeight: 80 }}>
      <div style={{ marginTop: -6 }}>
        <window.Viz t={t} accent={accent} mood={mood} size={58} talking={!!speech} />
      </div>
      <div style={{ flex: 1, minWidth: 0, paddingTop: 6 }}>
        <div>
          {speech ? (
            <window.VizBubble t={t} accent={accent}>{speech}</window.VizBubble>
          ) : (
            <div style={{ animation: 'viz-fade-in 0.3s ease' }}>
              <div style={{
                fontFamily: t.monoFont, fontSize: 9.5, color: t.muted,
                letterSpacing: 1.6, fontWeight: 700,
              }}>TELSIZ.OKULU · {lvl.isim.toUpperCase()}</div>
              <div style={{
                display: 'flex', alignItems: 'baseline', gap: 8, marginTop: 2,
              }}>
                <span style={{
                  fontFamily: t.monoFont, fontWeight: 800, fontSize: 14,
                  color: t.ink, letterSpacing: -0.3,
                }}>{state.callsign || 'TA2/CALL'}</span>
                <span style={{
                  fontFamily: t.monoFont, fontSize: 10.5, color: t.muted, letterSpacing: 0.5,
                }}>LV.{lvl.no} · {lvl.current}/{lvl.span} XP</span>
              </div>
            </div>
          )}
        </div>
        {/* Mini stats line under bubble */}
        <div style={{
          marginTop: 6, display: 'flex', alignItems: 'center', gap: 6,
          fontFamily: t.monoFont, fontSize: 10, color: t.muted, letterSpacing: 0.6, fontWeight: 600,
        }}>
          <span>LV.{lvl.no}</span>
          <span style={{ color: t.hairline2 }}>•</span>
          <span>{lvl.current}<span style={{ color: t.mute2 }}>/{lvl.span}</span> XP</span>
          <div style={{ flex: 1, height: 3, marginLeft: 2, borderRadius: 2, background: t.hairline, overflow: 'hidden' }}>
            <div style={{
              width: `${lvl.pct * 100}%`, height: '100%', background: accent,
              transition: 'width 0.6s ease',
            }}/>
          </div>
        </div>
      </div>
      <div style={{ display: 'flex', flexDirection: 'column', gap: 6, paddingTop: 4 }}>
        <StatChip t={t} kind="streak" value={state.streak} />
        <StatChip t={t} kind="xp" value={state.xp} />
      </div>
    </div>
  );
}

function RadioLogo({ t }) {
  return (
    <div style={{
      width: 38, height: 38, borderRadius: 11, flexShrink: 0,
      border: `1px solid ${t.hairline2}`, background: t.surface2,
      display: 'grid', placeItems: 'center', position: 'relative',
      boxShadow: `inset 0 1px 0 ${t.surface}`,
    }}>
      <svg width="22" height="22" viewBox="0 0 24 24" fill="none">
        <path d="M2 14c2-4 6-7 10-7s8 3 10 7" stroke={t.accent} strokeWidth="1.6" strokeLinecap="round"/>
        <path d="M5 16c1.5-3 4-5 7-5s5.5 2 7 5" stroke={t.accent} strokeWidth="1.6" strokeLinecap="round" opacity=".55"/>
        <path d="M8 18c1-2 2.5-3 4-3s3 1 4 3" stroke={t.accent} strokeWidth="1.6" strokeLinecap="round" opacity=".3"/>
        <circle cx="12" cy="20" r="1.6" fill={t.accent}/>
      </svg>
    </div>
  );
}

function StatChip({ t, kind, value }) {
  const isStreak = kind === 'streak';
  const fg = isStreak ? t.streak : t.accent;
  return (
    <div style={{
      display: 'flex', alignItems: 'center', gap: 5,
      padding: '5px 9px', borderRadius: 999,
      background: fg + '14', border: `1px solid ${fg}30`,
    }}>
      <svg width="12" height="12" viewBox="0 0 24 24" fill="none">
        {isStreak
          ? <path d="M12 3c1 3-2 4-2 7 0 2 1 4 3 4-1 0-2 2-1 4-3 0-6-3-6-7 0-3 2-5 3-6 1 1 2 1 3-2z" fill={fg}/>
          : <path d="M12 2l2.6 6.4L21 9.3l-5 4.3 1.6 6.6L12 16.8 6.4 20.2 8 13.6 3 9.3l6.4-.9L12 2z" fill={fg}/>
        }
      </svg>
      <span style={{ fontFamily: t.monoFont, fontSize: 11, fontWeight: 700, color: fg }}>{value}</span>
    </div>
  );
}

// ──────────────────────────────────────────────────────────────
// Frequency dial — horizontal radio-tuner chapter switcher
// ──────────────────────────────────────────────────────────────
function FrequencyDial({ t, cur, selIdx, state, onSelect, isLocked }) {
  return (
    <div style={{ position: 'relative' }}>
      <div style={{
        display: 'flex', alignItems: 'center', gap: 8,
      }}>
        <DialArrow t={t} dir="left" disabled={selIdx === 0} onClick={() => onSelect(selIdx - 1)} />

        <div style={{
          flex: 1, position: 'relative',
          background: t.surface2, borderRadius: 9,
          border: `1px solid ${t.hairline}`,
          padding: '7px 4px 4px',
          overflow: 'hidden',
        }}>
          {/* tick scale */}
          <div style={{
            display: 'flex', justifyContent: 'space-between', padding: '0 6px',
          }}>
            {Array.from({ length: 25 }).map((_, i) => (
              <span key={i} style={{
                width: 1, height: i % 4 === 0 ? 6 : 3,
                background: t.hairline2, opacity: 0.7, display: 'block',
              }}/>
            ))}
          </div>
          {/* chapter cells */}
          <div style={{
            display: 'grid', gridTemplateColumns: `repeat(${cur.length}, 1fr)`, gap: 2,
            marginTop: 4,
          }}>
            {cur.map((b, i) => {
              const locked = isLocked(i);
              const p = window.chapterProgress(b, state.ilerleme);
              const isActive = i === selIdx;
              const done = p.done === p.total;
              return (
                <button key={b.id} onClick={() => onSelect(i)} style={{
                  position: 'relative',
                  padding: '6px 2px 7px', borderRadius: 6,
                  background: isActive ? b.renk : 'transparent',
                  color: isActive ? '#fff' : locked ? t.mute2 : (done ? b.renk : t.ink2),
                  border: 'none', cursor: locked ? 'not-allowed' : 'pointer',
                  fontFamily: t.monoFont, fontWeight: 800, fontSize: 11,
                  letterSpacing: 0.5, textAlign: 'center',
                  transition: 'background 0.22s ease, color 0.22s ease, transform 0.18s',
                  transform: isActive ? 'translateY(-1px)' : 'translateY(0)',
                  boxShadow: isActive ? `0 4px 10px ${b.renk}55` : 'none',
                }}>
                  <div style={{ opacity: locked && !isActive ? 0.45 : 1 }}>
                    {String(b.no).padStart(2,'0')}
                  </div>
                  <div style={{
                    height: 2, marginTop: 4, marginInline: 4, borderRadius: 1,
                    background: isActive ? `rgba(255,255,255,0.5)`
                      : done ? b.renk
                      : locked ? t.hairline2
                      : b.renk + '50',
                    transform: `scaleX(${isActive ? 1 : Math.max(0.15, p.pct)})`,
                    transformOrigin: 'left',
                    transition: 'transform 0.3s ease',
                  }}/>
                </button>
              );
            })}
          </div>
        </div>

        <DialArrow t={t} dir="right" disabled={selIdx === cur.length - 1} onClick={() => onSelect(selIdx + 1)} />
      </div>
    </div>
  );
}

function DialArrow({ t, dir, disabled, onClick }) {
  return (
    <button onClick={onClick} disabled={disabled} style={{
      width: 30, height: 30, borderRadius: 8, flexShrink: 0,
      background: disabled ? 'transparent' : t.surface2,
      border: `1px solid ${disabled ? 'transparent' : t.hairline2}`,
      color: disabled ? t.hairline2 : t.ink2,
      cursor: disabled ? 'default' : 'pointer',
      display: 'grid', placeItems: 'center',
      fontFamily: t.monoFont, fontWeight: 700, fontSize: 14,
    }}>
      {dir === 'left' ? '‹' : '›'}
    </button>
  );
}

// Segmented chapter progress — one cell per lesson
function SegmentedProgress({ t, bolum, state, style }) {
  return (
    <div style={{ display: 'flex', gap: 4, ...style }}>
      {bolum.dersler.map((d) => {
        const s = state.ilerleme[d.id] || 'locked';
        return (
          <div key={d.id} style={{
            flex: 1, height: 5, borderRadius: 2,
            background: s === 'done' ? bolum.renk
              : s === 'current' ? bolum.renk + '55'
              : t.hairline,
            position: 'relative', overflow: 'hidden',
          }}>
            {s === 'current' && (
              <div style={{
                position: 'absolute', inset: 0,
                background: `linear-gradient(90deg, ${bolum.renk} 50%, transparent 50%)`,
                backgroundSize: '8px 100%',
                animation: 'pulse-stripe 1.8s linear infinite',
              }}/>
            )}
          </div>
        );
      })}
    </div>
  );
}

// Big CTA — "transmit" button with subtle waveform + ripple
function TransmitButton({ t, bolum, ders, onClick, onHover, ripple }) {
  const [pressed, setPressed] = hState(false);
  return (
    <button
      onClick={onClick}
      onMouseEnter={onHover}
      onPointerDown={() => setPressed(true)}
      onPointerUp={() => setPressed(false)}
      onPointerLeave={() => setPressed(false)}
      style={{
        marginTop: 14, width: '100%',
        display: 'flex', alignItems: 'center', gap: 12,
        padding: '12px 14px',
        background: t.ink, color: t.surface,
        border: 'none', borderRadius: t.radius,
        cursor: 'pointer', textAlign: 'left',
        position: 'relative', overflow: 'hidden',
        boxShadow: pressed
          ? `0 2px 6px ${bolum.renk}40, inset 0 0 0 1px ${bolum.renk}80`
          : `0 6px 18px ${bolum.renk}30`,
        transform: pressed ? 'translateY(1px) scale(0.997)' : 'translateY(0) scale(1)',
        transition: 'transform 0.12s ease, box-shadow 0.18s ease',
    }}>
      {/* click ripple */}
      {ripple && (
        <span key={ripple} style={{
          position: 'absolute', top: '50%', left: 36, width: 40, height: 40,
          borderRadius: '50%', background: bolum.renk,
          pointerEvents: 'none',
          animation: 'transmit-ripple 0.7s ease-out',
          '--glow': bolum.renk,
        }}/>
      )}

      {/* tiny waveform decoration */}
      <svg width="78" height="34" viewBox="0 0 78 34" style={{
        position: 'absolute', right: 44, top: '50%', transform: 'translateY(-50%)',
        opacity: 0.35, pointerEvents: 'none',
      }}>
        {Array.from({ length: 22 }).map((_, i) => {
          const h = 4 + Math.abs(Math.sin(i * 0.55) * 14);
          return <rect key={i} x={i * 3.5} y={17 - h/2} width="1.4" height={h} fill={bolum.renk} rx="0.5"
            style={{
              transformBox: 'fill-box', transformOrigin: 'center',
              animation: `spectrum-wave ${1.4 + (i % 4) * 0.3}s ease-in-out infinite`,
              animationDelay: `${i * 0.05}s`,
            }}/>;
        })}
      </svg>

      <div style={{
        width: 40, height: 40, borderRadius: 10, flexShrink: 0,
        background: bolum.renk,
        display: 'grid', placeItems: 'center',
        boxShadow: `inset 0 1px 0 rgba(255,255,255,0.25)`,
        position: 'relative',
      }}>
        <svg width="14" height="14" viewBox="0 0 12 12"><path d="M3 2l7 4-7 4V2z" fill="#fff"/></svg>
      </div>
      <div style={{ flex: 1, minWidth: 0, position: 'relative' }}>
        <div style={{
          fontFamily: t.monoFont, fontSize: 9.5, color: bolum.renk,
          letterSpacing: 1.6, fontWeight: 700,
        }}>
          ▸ TRANSMIT · {ders.sure} DK
        </div>
        <div style={{
          fontSize: 14.5, fontWeight: 700, color: t.bg,
          marginTop: 2, letterSpacing: -0.2,
          overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap',
        }}>{ders.baslik}</div>
      </div>
      <span style={{
        color: bolum.renk, fontSize: 22, lineHeight: 1, flexShrink: 0,
        position: 'relative',
        transform: pressed ? 'translateX(2px)' : 'translateX(0)',
        transition: 'transform 0.15s',
      }}>→</span>
    </button>
  );
}

// ──────────────────────────────────────────────────────────────
// Exam strip — T-minus + weak topics in a single thin row
// ──────────────────────────────────────────────────────────────
function ExamStrip({ t, state, go }) {
  const examColor = state.examReady > 0.7 ? t.success : state.examReady > 0.4 ? t.warn : t.danger;
  return (
    <div style={{
      margin: '12px 16px 14px',
      display: 'flex', alignItems: 'stretch', gap: 8,
    }}>
      <button onClick={() => go({ screen: 'sinav' })} style={{
        flex: 1, display: 'flex', alignItems: 'center', gap: 10,
        padding: '9px 12px', borderRadius: t.radiusSm,
        background: t.surface, border: `1px solid ${t.hairline}`,
        cursor: 'pointer', textAlign: 'left',
      }}>
        <div style={{
          fontFamily: t.monoFont, fontWeight: 800, fontSize: 13,
          color: t.ink, letterSpacing: -0.4,
        }}>T‑{String(state.examDays).padStart(2,'0')}</div>
        <div style={{ flex: 1, display: 'flex', flexDirection: 'column', gap: 3 }}>
          <div style={{
            display: 'flex', justifyContent: 'space-between',
            fontFamily: t.monoFont, fontSize: 9.5, color: t.muted, letterSpacing: 1,
          }}>
            <span>TRAC HAZIR</span>
            <span style={{ color: examColor, fontWeight: 700 }}>{Math.round(state.examReady*100)}%</span>
          </div>
          <div style={{ height: 3, borderRadius: 2, background: t.hairline, overflow: 'hidden' }}>
            <div style={{ width: `${state.examReady * 100}%`, height: '100%', background: examColor }} />
          </div>
        </div>
      </button>

      {state.zayifKonu > 0 && (
        <button onClick={() => go({ screen: 'pratik' })} style={{
          padding: '0 12px', borderRadius: t.radiusSm,
          background: t.streakTint, border: `1px dashed ${t.streak}55`,
          display: 'flex', alignItems: 'center', gap: 6, cursor: 'pointer', flexShrink: 0,
        }}>
          <svg width="11" height="11" viewBox="0 0 24 24" fill="none">
            <path d="M12 3c1 3-2 4-2 7 0 2 1 4 3 4-1 0-2 2-1 4-3 0-6-3-6-7 0-3 2-5 3-6 1 1 2 1 3-2z" fill={t.streak}/>
          </svg>
          <span style={{ fontFamily: t.monoFont, fontWeight: 700, fontSize: 11, color: t.streak, letterSpacing: 0.5 }}>
            {state.zayifKonu} ZAYIF
          </span>
        </button>
      )}
    </div>
  );
}

// ──────────────────────────────────────────────────────────────
// Section header for lesson list
// ──────────────────────────────────────────────────────────────
function SectionHeader({ t, bolum, prog }) {
  return (
    <div style={{
      display: 'flex', alignItems: 'baseline', justifyContent: 'space-between',
      padding: '4px 2px 12px',
    }}>
      <div style={{ display: 'flex', alignItems: 'baseline', gap: 8 }}>
        <div style={{
          fontFamily: t.monoFont, fontSize: 10.5, letterSpacing: 1.8,
          color: t.ink2, fontWeight: 700,
        }}>DERSLER</div>
        <div style={{ fontFamily: t.monoFont, fontSize: 10.5, color: t.muted }}>
          {prog.done}/{prog.total}
        </div>
      </div>
      <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
        <div style={{ width: 56, height: 3, borderRadius: 2, background: t.hairline, overflow: 'hidden' }}>
          <div style={{ width: `${prog.pct * 100}%`, height: '100%', background: bolum.renk }} />
        </div>
        <span style={{ fontFamily: t.monoFont, fontSize: 10, color: t.muted, minWidth: 28, textAlign: 'right' }}>
          {Math.round(prog.pct * 100)}%
        </span>
      </div>
    </div>
  );
}

// ──────────────────────────────────────────────────────────────
// Lesson track — vertical timeline of lessons
// ──────────────────────────────────────────────────────────────
function LessonTrack({ t, bolum, state, isChapterLocked, onClick }) {
  return (
    <div style={{ position: 'relative' }}>
      {bolum.dersler.map((d, i) => {
        const status = state.ilerleme[d.id] || 'locked';
        const prevDone = i === 0 || state.ilerleme[bolum.dersler[i - 1].id] === 'done';
        const accessible = !isChapterLocked && (status === 'done' || prevDone);
        const isLast = i === bolum.dersler.length - 1;
        return (
          <TrackRow key={d.id}
            ders={d} status={status} accessible={accessible}
            t={t} renk={bolum.renk} no={i + 1}
            isLast={isLast}
            onClick={() => accessible && onClick(d)}
          />
        );
      })}
    </div>
  );
}

function TrackRow({ ders, status, accessible, t, renk, no, isLast, onClick }) {
  const isExam = !!ders.sinav;
  const done = status === 'done';
  const current = status === 'current';
  const [hover, setHover] = hState(false);

  return (
    <div style={{
      display: 'flex', alignItems: 'stretch', gap: 10, position: 'relative',
      animation: `viz-fade-in 0.3s ease both`,
      animationDelay: `${no * 0.04}s`,
    }}>
      {/* Track column */}
      <div style={{
        width: 36, position: 'relative', flexShrink: 0,
        display: 'flex', flexDirection: 'column', alignItems: 'center',
      }}>
        {/* line above the dot */}
        {no > 1 && (
          <div style={{
            position: 'absolute', top: 0, height: 12, width: 2,
            background: done || current ? renk + '55' : t.hairline2,
            borderRadius: 1,
          }}/>
        )}
        {/* dot */}
        <div style={{
          marginTop: 10, width: isExam ? 28 : 26, height: isExam ? 28 : 26,
          borderRadius: isExam ? 7 : 14,
          background: done ? renk : current ? t.surface : accessible ? t.surface : t.hairline,
          border: `2px solid ${done ? renk : current ? renk : accessible ? renk + '66' : t.hairline2}`,
          display: 'grid', placeItems: 'center', flexShrink: 0,
          color: done ? '#fff' : current ? renk : accessible ? renk : t.mute2,
          fontFamily: t.monoFont, fontWeight: 800, fontSize: 10.5,
          position: 'relative', zIndex: 1,
          boxShadow: current ? `0 0 0 4px ${renk}22` : 'none',
        }}>
          {!accessible
            ? <LockGlyph color={t.mute2}/>
            : done
              ? <CheckGlyph color="#fff"/>
              : isExam ? 'Q' : String(no).padStart(2, '0')}
        </div>
        {/* current ping */}
        {current && (
          <div style={{
            position: 'absolute', top: 10, width: 28, height: 28, borderRadius: 14,
            border: `2px solid ${renk}`,
            animation: 'ping 1.6s ease-out infinite',
            pointerEvents: 'none',
          }}/>
        )}
        {/* line below the dot */}
        {!isLast && (
          <div style={{
            flex: 1, marginTop: 2, width: 2,
            background: done ? renk + '55' : t.hairline2,
            borderRadius: 1, minHeight: 18,
          }}/>
        )}
      </div>

      {/* Row card */}
      <div
        onClick={accessible ? onClick : undefined}
        onMouseEnter={() => setHover(true)}
        onMouseLeave={() => setHover(false)}
        style={{
          flex: 1, marginBottom: isLast ? 4 : 8, marginTop: 4,
          display: 'flex', alignItems: 'center', gap: 10,
          padding: '11px 13px',
          background: current ? renk + '12' : t.surface,
          border: `1px solid ${current ? renk + '55' : hover && accessible ? renk + '40' : t.hairline}`,
          borderRadius: t.radiusSm,
          cursor: accessible ? 'pointer' : 'not-allowed',
          opacity: accessible ? 1 : 0.55,
          transform: hover && accessible ? 'translateX(2px)' : 'translateX(0)',
          transition: 'transform 0.15s ease, border-color 0.18s ease, background 0.18s',
        }}>
        <div style={{ flex: 1, minWidth: 0 }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 7, flexWrap: 'wrap' }}>
            <span style={{ fontWeight: 700, fontSize: 14.5, color: t.ink, letterSpacing: -0.2 }}>
              {ders.baslik}
            </span>
            {isExam && (
              <span style={{
                fontFamily: t.monoFont, fontSize: 9, fontWeight: 700, letterSpacing: 1,
                padding: '2px 6px', borderRadius: 4,
                background: t.streakTint, color: t.streak,
              }}>SINAV</span>
            )}
            {current && (
              <span style={{
                fontFamily: t.monoFont, fontSize: 9, fontWeight: 700, letterSpacing: 1,
                padding: '2px 6px', borderRadius: 4,
                background: renk, color: '#fff',
              }}>AKTİF</span>
            )}
          </div>
          <div style={{
            fontSize: 11.5, color: t.muted, marginTop: 3, lineHeight: 1.35,
            overflow: 'hidden', display: '-webkit-box',
            WebkitLineClamp: 1, WebkitBoxOrient: 'vertical',
          }}>{ders.alt}</div>
        </div>
        <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-end', gap: 4, flexShrink: 0 }}>
          <div style={{ fontFamily: t.monoFont, fontSize: 11, color: t.muted }}>
            {ders.sure} dk
          </div>
          {accessible && (
            <span style={{
              color: current ? renk : hover ? renk : t.muted,
              fontSize: 14, lineHeight: 1,
              transform: hover ? 'translateX(2px)' : 'translateX(0)',
              transition: 'transform 0.15s, color 0.18s',
            }}>→</span>
          )}
        </div>
      </div>
    </div>
  );
}

function CheckGlyph({ color }) {
  return (
    <svg width="12" height="12" viewBox="0 0 12 12" fill="none">
      <path d="M2.5 6.2L4.8 8.4 9.5 3.4" stroke={color} strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
    </svg>
  );
}
function LockGlyph({ color }) {
  return (
    <svg width="11" height="11" viewBox="0 0 12 12" fill="none">
      <rect x="2.5" y="5.5" width="7" height="5" rx="1" stroke={color} strokeWidth="1.4"/>
      <path d="M4 5.5V4a2 2 0 014 0v1.5" stroke={color} strokeWidth="1.4"/>
    </svg>
  );
}

// Inject keyframes once
if (typeof document !== 'undefined' && !document.getElementById('home-anim')) {
  const s = document.createElement('style');
  s.id = 'home-anim';
  s.textContent = `
    @keyframes ping { 0% { transform: scale(0.9); opacity: 0.8; } 100% { transform: scale(1.6); opacity: 0; } }
    @keyframes pulse-stripe { 0% { background-position: 0 0; } 100% { background-position: 16px 0; } }
  `;
  document.head.appendChild(s);
}

// ──────────────────────────────────────────────────────────────
// Level helper
// ──────────────────────────────────────────────────────────────
function level(xp) {
  const tbl = [
    { min: 0,    next: 200,  no: 1, isim: 'Acemi Telsizci' },
    { min: 200,  next: 500,  no: 2, isim: 'Çırak Operatör' },
    { min: 500,  next: 1000, no: 3, isim: 'Junior Operatör' },
    { min: 1000, next: 2000, no: 4, isim: 'Operatör' },
    { min: 2000, next: 3500, no: 5, isim: 'Senior Operatör' },
    { min: 3500, next: 5000, no: 6, isim: 'Usta Operatör' },
    { min: 5000, next: 5000, no: 7, isim: 'TRAC Adayı' },
  ];
  for (let i = tbl.length - 1; i >= 0; i--) {
    if (xp >= tbl[i].min) {
      const o = tbl[i];
      const span = (o.next - o.min) || 1;
      const pct = Math.min(1, (xp - o.min) / span);
      return { ...o, pct, current: xp - o.min, span };
    }
  }
  return tbl[0];
}
window.level = level;

// ──────────────────────────────────────────────────────────────
// SpectrumBg — still exported for chapter showcases
// ──────────────────────────────────────────────────────────────
function SpectrumBg({ t, accent }) {
  const bars = 60;
  return (
    <div style={{
      position: 'absolute', inset: 0,
      background: `linear-gradient(180deg, ${accent}10 0%, transparent 70%)`,
      pointerEvents: 'none',
    }}>
      <svg width="100%" height="50" preserveAspectRatio="none" style={{ display: 'block' }}>
        {Array.from({ length: bars }).map((_, i) => {
          const h = 7 + Math.abs(Math.sin(i * 0.42) * 13) + (i % 7 === 0 ? 12 : 0);
          return <rect key={i} x={`${(i / bars) * 100}%`} y={26 - h / 2}
            width="1.4" height={h} fill={accent} opacity={0.16 + (i % 5) * 0.04} />;
        })}
      </svg>
    </div>
  );
}
window.SpectrumBg = SpectrumBg;

// ──────────────────────────────────────────────────────────────
// Grill Me FAB — with mini-mascot face & breathing pulse
// ──────────────────────────────────────────────────────────────
function GrillMeButton({ t, onClick }) {
  const [hover, setHover] = hState(false);
  return (
    <div style={{
      position: 'fixed', bottom: 88, right: 16, zIndex: 50,
      display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 4,
    }}>
      {/* breathing ring */}
      <div style={{
        position: 'absolute', top: 0, width: 54, height: 54, borderRadius: 27,
        border: `2px solid ${t.streak}`, opacity: 0.5,
        animation: 'pulse 1.8s ease-out infinite',
        pointerEvents: 'none',
      }}/>
      <button
        onClick={onClick}
        onMouseEnter={() => setHover(true)}
        onMouseLeave={() => setHover(false)}
        style={{
          width: 54, height: 54, borderRadius: 27,
          background: t.streak, border: 'none', cursor: 'pointer',
          boxShadow: `0 6px 22px ${t.streak}55, inset 0 1px 0 rgba(255,255,255,0.3)`,
          display: 'grid', placeItems: 'center',
          transition: 'transform 0.15s cubic-bezier(.34,1.56,.64,1)',
          transform: hover ? 'scale(1.08) rotate(-6deg)' : 'scale(1)',
          position: 'relative', overflow: 'hidden',
        }}>
        <svg width="26" height="26" viewBox="0 0 24 24" fill="none">
          <path d="M13 2L4 14h7l-1 8 9-12h-7l1-8z"
            fill="#fff" stroke="#fff" strokeWidth="0.6" strokeLinejoin="round"/>
        </svg>
      </button>
      <div style={{
        fontFamily: t.monoFont, fontSize: 9, color: t.muted, fontWeight: 700, letterSpacing: 0.6,
      }}>DRILL</div>
    </div>
  );
}

// ──────────────────────────────────────────────────────────────
// Grill Me Modal
// ──────────────────────────────────────────────────────────────
function GrillMeModal({ t, bolum, onClose }) {
  const [q] = hState(() => randomGrillQuestion(bolum));
  const [picked, setPicked] = hState(null);
  const [reveal, setReveal] = hState(false);
  const isCorrect = picked === q.correctIndex;

  return (
    <div style={{
      position: 'fixed', inset: 0, zIndex: 200,
      background: 'rgba(0,0,0,0.55)', display: 'flex', alignItems: 'flex-end',
    }} onClick={(e) => e.target === e.currentTarget && onClose()}>
      <div style={{
        width: '100%', maxWidth: 440, margin: '0 auto',
        background: t.surface, borderRadius: '20px 20px 0 0',
        padding: '20px 18px 32px',
        border: `1px solid ${t.hairline}`,
      }}>
        <div style={{ width: 36, height: 4, borderRadius: 2, background: t.hairline2, margin: '0 auto 18px' }} />

        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 14 }}>
          <div style={{ fontFamily: t.monoFont, fontSize: 10.5, color: t.streak, fontWeight: 700, letterSpacing: 1.6 }}>
            ⚡ GRILL ME · {bolum.kod}
          </div>
          <button onClick={onClose} style={{
            width: 28, height: 28, borderRadius: 7,
            background: t.surface2, border: `1px solid ${t.hairline}`,
            cursor: 'pointer', color: t.ink, fontSize: 15,
            display: 'grid', placeItems: 'center',
          }}>×</button>
        </div>

        <div style={{ fontSize: 18, fontWeight: 800, color: t.ink, letterSpacing: -0.3, lineHeight: 1.3, marginBottom: 12 }}>
          {q.prompt}
        </div>
        {q.subtitle && (
          <div style={{
            padding: 12, borderRadius: 8, marginBottom: 14,
            background: t.surface2, border: `1px solid ${t.hairline}`,
            fontFamily: t.monoFont, fontSize: 28, fontWeight: 700,
            color: bolum.renk, letterSpacing: 2, textAlign: 'center',
          }}>{q.subtitle}</div>
        )}

        <div style={{ display: 'grid', gap: 7 }}>
          {q.options.map((opt, i) => {
            const isPicked = picked === i;
            const showOk = reveal && i === q.correctIndex;
            const showBad = reveal && isPicked && !isCorrect;
            return (
              <button key={i} disabled={reveal} onClick={() => setPicked(i)} style={{
                display: 'flex', alignItems: 'center', gap: 10,
                padding: '11px 13px', borderRadius: 10, textAlign: 'left',
                background: showOk ? t.success + '14' : showBad ? t.danger + '14' : isPicked ? t.accentTint : t.surface2,
                border: `1.5px solid ${showOk ? t.success : showBad ? t.danger : isPicked ? t.accent : t.hairline2}`,
                cursor: reveal ? 'default' : 'pointer',
              }}>
                <span style={{
                  fontFamily: t.monoFont, fontWeight: 700, fontSize: 11,
                  color: showOk ? t.success : showBad ? t.danger : isPicked ? t.accent : t.muted,
                  minWidth: 18,
                }}>{String.fromCharCode(65 + i)}</span>
                <span style={{ flex: 1, fontWeight: 600, fontSize: 13.5, color: t.ink }}>{opt}</span>
                {showOk && <span style={{ color: t.success, fontWeight: 800 }}>✓</span>}
                {showBad && <span style={{ color: t.danger, fontWeight: 800 }}>✕</span>}
              </button>
            );
          })}
        </div>

        <div style={{ marginTop: 14 }}>
          {reveal && (
            <div style={{ marginBottom: 8, fontWeight: 700, fontSize: 13, color: isCorrect ? t.success : t.danger }}>
              {isCorrect ? '✓ Doğru! Harika.' : `✕ Doğru cevap: ${q.options[q.correctIndex]}`}
            </div>
          )}
          <button
            disabled={picked === null}
            onClick={() => { if (!reveal) setReveal(true); else onClose(); }}
            style={{
              width: '100%', padding: 13,
              background: picked === null ? t.hairline : t.ink,
              color: picked === null ? t.mute2 : t.surface,
              border: 'none', borderRadius: 11,
              fontWeight: 700, fontSize: 14, cursor: picked === null ? 'not-allowed' : 'pointer',
            }}>
            {reveal ? 'Kapat' : 'Kontrol et'}
          </button>
        </div>
      </div>
    </div>
  );
}

function randomGrillQuestion(bolum) {
  if (bolum.id === 'b1') {
    const N = window.NATO;
    const idx = Math.floor(Math.random() * N.length);
    const correct = N[idx];
    const others = N.filter((_, i) => i !== idx).sort(() => Math.random() - 0.5).slice(0, 3);
    const opts = [correct, ...others].sort(() => Math.random() - 0.5);
    return { prompt: `"${correct[0]}" harfinin NATO karşılığı?`, subtitle: correct[0], options: opts.map(r => r[1]), correctIndex: opts.indexOf(correct) };
  }
  if (bolum.id === 'b2') {
    const Q = window.QKODES;
    const idx = Math.floor(Math.random() * Q.length);
    const [kod, anlam] = Q[idx];
    const others = Q.filter((_, i) => i !== idx).sort(() => Math.random() - 0.5).slice(0, 3).map(x => x[1]);
    const opts = [anlam, ...others].sort(() => Math.random() - 0.5);
    return { prompt: `${kod} ne anlama gelir?`, subtitle: kod, options: opts, correctIndex: opts.indexOf(anlam) };
  }
  const pools = {
    b3: [
      { p: 'Ohm kanunu formülü?', opts: ['V = I·R', 'V = I/R', 'P = V·I', 'R = V+I'], a: 0 },
      { p: 'Güç formülü?', opts: ['P = V·I', 'P = V/I', 'P = I/V', 'P = V+I'], a: 0 },
      { p: '12V / 4Ω = ?', opts: ['2A', '3A', '4A', '6A'], a: 1, s: 'V/R' },
    ],
    b4: [
      { p: '20m bandı kaç MHz?', opts: ['7', '14', '21', '28'], a: 1, s: '20m' },
      { p: '2m bandı kaç MHz?', opts: ['50', '144', '435', '1296'], a: 1, s: '2m' },
      { p: 'Dalga boyu formülü?', opts: ['λ=c/f', 'λ=c·f', 'λ=f/c', 'λ=c²/f'], a: 0 },
    ],
    b5: [
      { p: '"Anlaşıldı" kısaltması?', opts: ['Over', 'Roger', 'Wilco', 'Break'], a: 1 },
      { p: 'Türkiye ön eki?', opts: ['DL', 'F', 'TA', 'G'], a: 2 },
      { p: 'Acil durum çağrısı?', opts: ['CQ', 'QRZ', 'MAYDAY', '73'], a: 2 },
    ],
    b6: [
      { p: 'TRAC neyin kısaltması?', opts: ['Türkiye Radyo Amatörleri Cemiyeti', 'Telsiz Radyo A.C.', 'Teknik Radyo A.C.', 'T.R.A.C.'], a: 0 },
      { p: 'Sınav geçme eşiği?', opts: ['50%', '60%', '70%', '80%'], a: 1 },
    ],
  };
  const pool = pools[bolum.id] || pools.b5;
  const q = pool[Math.floor(Math.random() * pool.length)];
  return { prompt: q.p, subtitle: q.s || null, options: q.opts, correctIndex: q.a };
}

// ──────────────────────────────────────────────────────────────
// Toast
// ──────────────────────────────────────────────────────────────
function Toast({ t, message }) {
  return (
    <div style={{
      position: 'fixed', bottom: 96, left: '50%', transform: 'translateX(-50%)',
      zIndex: 300, whiteSpace: 'nowrap',
      padding: '11px 18px', borderRadius: 20,
      background: t.ink, color: t.surface,
      fontWeight: 600, fontSize: 12.5,
      boxShadow: '0 4px 18px rgba(0,0,0,0.25)',
      border: `1px solid ${t.hairline}`,
    }}>
      {message}
    </div>
  );
}

// ──────────────────────────────────────────────────────────────
// Daily Morse
// ──────────────────────────────────────────────────────────────
function DailyMorse({ t }) {
  const idx = new Date().getDate() % window.GUNUN_BILGISI.length;
  const gun = window.GUNUN_BILGISI[idx];
  return (
    <div style={{ marginTop: 14, marginBottom: 0 }}>
      <window.Card t={t} style={{ padding: 14 }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <window.Mono t={t}>GÜNÜN BİLGİSİ</window.Mono>
          <window.Mono t={t} style={{ fontWeight: 700, color: t.accent }}>{gun.kod}</window.Mono>
        </div>
        <div style={{
          marginTop: 10, padding: '10px 12px',
          background: t.surface2, borderRadius: 8,
          border: `1px solid ${t.hairline}`,
          fontFamily: t.monoFont, fontSize: 14, color: t.ink,
          letterSpacing: 2, textAlign: 'center', fontWeight: 600,
        }}>
          {morseEncode(gun.kod)}
        </div>
        <div style={{ fontSize: 12.5, color: t.ink2, marginTop: 8, lineHeight: 1.45 }}>{gun.metin}</div>
      </window.Card>
    </div>
  );
}

function morseEncode(text) {
  const m = { A:'.-',B:'-...',C:'-.-.',D:'-..',E:'.',F:'..-.',G:'--.',H:'....',I:'..',J:'.---',K:'-.-',L:'.-..',M:'--',N:'-.',O:'---',P:'.--.',Q:'--.-',R:'.-.',S:'...',T:'-',U:'..-',V:'...-',W:'.--',X:'-..-',Y:'-.--',Z:'--..',0:'-----',1:'.----',2:'..---',3:'...--',4:'....-',5:'.....',6:'-....',7:'--...',8:'---..',9:'----.' };
  return text.toUpperCase().split('').map(c => m[c] || ' ').join(' / ');
}

// Shared primitives
function Mono({ t, children, style }) {
  return <div style={{ fontFamily: t.monoFont, fontSize: 10, letterSpacing: 1.4, color: t.muted, fontWeight: 600, ...style }}>{children}</div>;
}
function Card({ t, children, style, onClick }) {
  return <div onClick={onClick} style={{ background: t.surface, border: `1px solid ${t.hairline}`, borderRadius: t.radius, ...style }}>{children}</div>;
}
function Bar({ t, pct, color, style, height = 6 }) {
  return (
    <div style={{ height, borderRadius: height / 2, background: t.hairline, overflow: 'hidden', ...style }}>
      <div style={{ width: `${Math.min(1, pct) * 100}%`, height: '100%', background: color, borderRadius: height / 2 }} />
    </div>
  );
}

function chapterProgress(bolum, ilerleme) {
  const total = bolum.dersler.length;
  let done = 0;
  for (const l of bolum.dersler) if ((ilerleme[l.id] || 'locked') === 'done') done++;
  return { done, total, pct: done / total };
}

Object.assign(window, { Home, Mono, Card, Bar, SpectrumBg, chapterProgress });
