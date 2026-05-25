// Secondary screens: Bolum detail, Lesson exercise, Sinav menu, Kutuphane, Profil
const { useMemo: u2Memo, useState: u2State, useEffect: u2Effect } = React;

// ──────────────────────────────────────────────────────────────
// Bolüm detay — list of lessons in this chapter
// ──────────────────────────────────────────────────────────────
function BolumDetay({ t, state, go, bolumId }) {
  const bolum = window.CURRICULUM.find(b => b.id === bolumId);
  if (!bolum) return null;
  const prog = window.chapterProgress(bolum, state.ilerleme);

  return (
    <div className="screen" style={{ background: t.bg }}>
      <ScreenHeader t={t} title={bolum.baslik} onBack={() => go({ screen: 'home' })}
        eyebrow={`BÖLÜM ${String(bolum.no).padStart(2,'0')} · ${bolum.kod}`} accent={bolum.renk}/>

      <div style={{ padding: '0 16px' }}>
        {window.ChapterShowcase && <window.ChapterShowcase t={t} bolum={bolum} state={state} />}

        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '4px 4px 10px' }}>
          <div style={{ fontFamily: t.monoFont, fontSize: 10.5, letterSpacing: 1.6, color: t.muted, fontWeight: 700 }}>
            DERSLER · {prog.done}/{prog.total} TAMAMLANDI
          </div>
          <div style={{ width: 70 }}>
            <window.Bar t={t} pct={prog.pct} color={bolum.renk} height={4}/>
          </div>
        </div>

        {bolum.dersler.map((d, i) => {
          const status = state.ilerleme[d.id] || 'locked';
          const prevDone = i === 0 || (state.ilerleme[bolum.dersler[i - 1].id] === 'done');
          const accessible = status === 'done' || status === 'current' || (status === 'locked' && prevDone);
          return (
            <LessonRow key={d.id}
              ders={d} status={status} accessible={accessible}
              t={t} renk={bolum.renk}
              onClick={() => accessible && go({ screen: 'lesson', bolumId: bolum.id, dersId: d.id })}
              isLast={i === bolum.dersler.length - 1}
              no={i + 1}
            />
          );
        })}

        <div style={{ height: 100 }} />
      </div>
    </div>
  );
}

function LessonRow({ ders, status, accessible, t, renk, onClick, isLast, no }) {
  const isExam = !!ders.sinav;
  const done = status === 'done';
  return (
    <div onClick={accessible ? onClick : undefined} style={{
      display: 'flex', gap: 12, alignItems: 'center',
      padding: '12px 14px', marginBottom: 8,
      background: t.surface, border: `1px solid ${t.hairline}`,
      borderLeft: `3px solid ${done ? renk : (accessible ? renk + '88' : t.hairline2)}`,
      borderRadius: t.radiusSm,
      cursor: accessible ? 'pointer' : 'not-allowed',
      opacity: accessible ? 1 : 0.55,
    }}>
      <div style={{
        width: 36, height: 36, borderRadius: isExam ? 8 : 18,
        background: done ? renk : (accessible ? renk + '15' : t.hairline),
        border: `1.5px solid ${done ? renk : (accessible ? renk + '40' : t.hairline2)}`,
        display: 'grid', placeItems: 'center', flexShrink: 0,
        color: done ? '#fff' : (accessible ? renk : t.mute2),
        fontFamily: t.monoFont, fontWeight: 700, fontSize: 12,
      }}>
        {!accessible ? '🔒' : (done ? '✓' : (isExam ? 'Q' : String(no).padStart(2,'0')))}
      </div>
      <div style={{ flex: 1, minWidth: 0 }}>
        <div style={{ display: 'flex', alignItems: 'baseline', gap: 8 }}>
          <div style={{ fontWeight: 700, fontSize: 14.5, color: t.ink, letterSpacing: -0.2 }}>{ders.baslik}</div>
          {isExam && (
            <span style={{
              fontFamily: t.monoFont, fontSize: 9.5, letterSpacing: 1, fontWeight: 700,
              padding: '2px 6px', borderRadius: 4,
              background: t.streakTint, color: t.streak,
            }}>SINAV</span>
          )}
        </div>
        <div style={{ fontSize: 12, color: t.muted, marginTop: 2 }}>{ders.alt}</div>
      </div>
      <div style={{ fontFamily: t.monoFont, fontSize: 11, color: t.muted }}>{ders.sure} dk</div>
    </div>
  );
}

// ──────────────────────────────────────────────────────────────
// Ders ekranı — interactive exercise (multiple choice)
// ──────────────────────────────────────────────────────────────
function Ders({ t, state, dispatch, go, bolumId, dersId }) {
  const bolum = window.CURRICULUM.find(b => b.id === bolumId);
  const ders = bolum?.dersler.find(d => d.id === dersId);
  if (!bolum || !ders) return null;

  // generate questions based on the bolum
  const questions = u2Memo(() => generateQuestions(bolum, ders), [dersId]);
  const [qi, setQi] = u2State(0);
  const [picked, setPicked] = u2State(null);
  const [reveal, setReveal] = u2State(false);
  const [correct, setCorrect] = u2State(0);

  if (qi >= questions.length) {
    return <DersBitti t={t} bolum={bolum} ders={ders} correct={correct} total={questions.length}
      onContinue={() => { dispatch({ type: 'complete-lesson', dersId, xp: 20 }); go({ screen: 'bolum', bolumId }); }} />;
  }

  const q = questions[qi];
  const dogruIdx = q.correctIndex;
  const isCorrect = picked === dogruIdx;

  return (
    <div className="screen" style={{ background: t.bg, display: 'flex', flexDirection: 'column' }}>
      {/* exit + progress */}
      <div style={{ padding: '14px 16px 8px', display: 'flex', alignItems: 'center', gap: 12 }}>
        <button onClick={() => go({ screen: 'bolum', bolumId })} style={{
          width: 36, height: 36, borderRadius: 10, border: `1px solid ${t.hairline}`,
          background: t.surface, color: t.ink, cursor: 'pointer', fontSize: 18,
        }}>×</button>
        <window.Bar t={t} pct={(qi) / questions.length} color={bolum.renk} style={{ flex: 1, height: 8 }} height={8}/>
        <div style={{ fontFamily: t.monoFont, fontSize: 11, color: t.muted, minWidth: 36, textAlign: 'right' }}>
          {qi + 1}/{questions.length}
        </div>
      </div>

      {/* question */}
      <div style={{ flex: 1, padding: '20px 18px', display: 'flex', flexDirection: 'column' }}>
        <div style={{ fontFamily: t.monoFont, fontSize: 10.5, letterSpacing: 1.6, color: bolum.renk, fontWeight: 700 }}>
          {q.tag}
        </div>
        <div style={{
          marginTop: 8, fontSize: 22, fontWeight: 800, color: t.ink,
          letterSpacing: -0.4, lineHeight: 1.25,
        }}>
          {q.prompt}
        </div>
        {q.subtitle && (
          <div style={{
            marginTop: 14, padding: '16px',
            background: t.surface, border: `1px solid ${t.hairline}`,
            borderRadius: t.radius, textAlign: 'center',
            fontFamily: t.monoFont, fontSize: 40, fontWeight: 700,
            color: bolum.renk, letterSpacing: 2,
          }}>
            {q.subtitle}
          </div>
        )}

        <div style={{ marginTop: 22, display: 'grid', gap: 10 }}>
          {q.options.map((opt, i) => {
            const isPicked = picked === i;
            const showCorrect = reveal && i === dogruIdx;
            const showWrong = reveal && isPicked && !isCorrect;
            const bg = showCorrect ? t.success + '15' : showWrong ? t.danger + '14' : (isPicked ? t.accentTint : t.surface);
            const bd = showCorrect ? t.success : showWrong ? t.danger : (isPicked ? t.accent : t.hairline2);
            return (
              <button key={i} disabled={reveal} onClick={() => setPicked(i)} style={{
                display: 'flex', alignItems: 'center', gap: 12,
                padding: '14px 14px', borderRadius: t.radius,
                background: bg, border: `1.5px solid ${bd}`,
                cursor: reveal ? 'default' : 'pointer', textAlign: 'left',
              }}>
                <div style={{
                  width: 28, height: 28, borderRadius: 8,
                  background: showCorrect ? t.success : showWrong ? t.danger : (isPicked ? t.accent : t.surface2),
                  border: `1.5px solid ${bd}`,
                  display: 'grid', placeItems: 'center',
                  fontFamily: t.monoFont, fontWeight: 700, fontSize: 12,
                  color: (showCorrect || showWrong || isPicked) ? '#fff' : t.ink,
                }}>{String.fromCharCode(65 + i)}</div>
                <div style={{ flex: 1, fontSize: 15, fontWeight: 600, color: t.ink }}>{opt}</div>
                {showCorrect && <span style={{ color: t.success, fontWeight: 800 }}>✓</span>}
                {showWrong && <span style={{ color: t.danger, fontWeight: 800 }}>✕</span>}
              </button>
            );
          })}
        </div>
      </div>

      {/* footer */}
      <div style={{
        padding: '14px 16px 18px',
        background: reveal ? (isCorrect ? t.success + '12' : t.danger + '12') : t.surface,
        borderTop: `1px solid ${t.hairline}`,
      }}>
        {reveal && (
          <div style={{ marginBottom: 10 }}>
            <div style={{ fontWeight: 800, color: isCorrect ? t.success : t.danger, fontSize: 14 }}>
              {isCorrect ? '✓ Doğru' : '✕ Yanlış'}
            </div>
            {!isCorrect && (
              <div style={{ fontSize: 13, color: t.ink2, marginTop: 2 }}>
                Doğru cevap: <b>{q.options[dogruIdx]}</b>
              </div>
            )}
          </div>
        )}
        <button
          disabled={picked === null}
          onClick={() => {
            if (!reveal) {
              setReveal(true);
              if (isCorrect) setCorrect(c => c + 1);
            } else {
              setReveal(false); setPicked(null); setQi(qi + 1);
            }
          }}
          style={{
            width: '100%', padding: '14px',
            background: picked === null ? t.hairline : t.ink, color: picked === null ? t.mute2 : t.surface,
            border: 'none', borderRadius: 12, fontWeight: 700, fontSize: 15,
            cursor: picked === null ? 'not-allowed' : 'pointer',
          }}>
          {reveal ? 'Devam et' : 'Kontrol et'}
        </button>
      </div>
    </div>
  );
}

function DersBitti({ t, bolum, ders, correct, total, onContinue }) {
  const pct = correct / total;
  const xp = Math.round(20 + correct * 5);
  return (
    <div className="screen" style={{ background: t.bg, display: 'flex', flexDirection: 'column' }}>
      <div style={{ flex: 1, padding: '40px 22px', display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', textAlign: 'center' }}>
        <div style={{
          width: 96, height: 96, borderRadius: 24,
          background: bolum.renk + '15', border: `2px solid ${bolum.renk}50`,
          display: 'grid', placeItems: 'center', marginBottom: 18,
        }}>
          <span style={{ fontFamily: t.monoFont, fontWeight: 800, fontSize: 32, color: bolum.renk }}>✓</span>
        </div>
        <div style={{ fontFamily: t.monoFont, fontSize: 11, letterSpacing: 1.6, color: bolum.renk, fontWeight: 700 }}>
          DERS TAMAMLANDI
        </div>
        <div style={{ marginTop: 6, fontSize: 24, fontWeight: 800, color: t.ink, letterSpacing: -0.4 }}>{ders.baslik}</div>

        <div style={{ marginTop: 26, display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: 10, width: '100%' }}>
          <StatBox t={t} label="DOĞRU" value={`${correct}/${total}`} />
          <StatBox t={t} label="BAŞARI" value={`${Math.round(pct * 100)}%`} />
          <StatBox t={t} label="+XP" value={`+${xp}`} hilite={t.accent}/>
        </div>
      </div>
      <div style={{ padding: '14px 16px 18px' }}>
        <button onClick={onContinue} style={{
          width: '100%', padding: 14, background: t.ink, color: t.surface,
          border: 'none', borderRadius: 12, fontWeight: 700, fontSize: 15, cursor: 'pointer',
        }}>Devam et</button>
      </div>
    </div>
  );
}

function StatBox({ t, label, value, hilite }) {
  return (
    <div style={{
      padding: '12px 8px', borderRadius: 12,
      background: t.surface, border: `1px solid ${t.hairline}`,
      textAlign: 'center',
    }}>
      <div style={{ fontFamily: t.monoFont, fontSize: 9.5, letterSpacing: 1.4, color: t.muted, fontWeight: 600 }}>{label}</div>
      <div style={{ fontFamily: t.monoFont, fontWeight: 800, fontSize: 20, color: hilite || t.ink, marginTop: 2 }}>{value}</div>
    </div>
  );
}

// generate small question set
function generateQuestions(bolum, ders) {
  // NATO chapter: letter ↔ phonetic
  if (bolum.id === 'b1') {
    const N = window.NATO;
    const pool = ders.alt.split(' · ').map(s => s.split(' ')[0]).filter(Boolean);
    return shuffled(pool).slice(0, 4).map((word, idx) => {
      const correct = N.find(r => r[1].toUpperCase() === word.toUpperCase()) || N[0];
      const distractors = shuffled(N.filter(r => r[1] !== correct[1])).slice(0, 3);
      const opts = shuffled([correct, ...distractors]).map(r => r[1]);
      return {
        tag: 'NATO ALFABESİ · ÇOKTAN SEÇMELİ',
        prompt: `"${correct[0]}" harfinin NATO karşılığı nedir?`,
        subtitle: correct[0],
        options: opts,
        correctIndex: opts.indexOf(correct[1]),
      };
    });
  }
  // Q codes
  if (bolum.id === 'b2') {
    const Q = window.QKODES;
    return shuffled(Q).slice(0, 4).map(([kod, anlam]) => {
      const distractors = shuffled(Q.filter(x => x[0] !== kod)).slice(0, 3).map(x => x[1]);
      const opts = shuffled([anlam, ...distractors]);
      return {
        tag: 'Q KODLARI · TANIMLAMA',
        prompt: `${kod} kodu ne anlama gelir?`,
        subtitle: kod,
        options: opts,
        correctIndex: opts.indexOf(anlam),
      };
    });
  }
  // Default: a small mixed set per chapter (canned)
  const map = {
    b3: [
      { p: 'Ohm kanunu nedir?', opts: ['V = I · R', 'V = I / R', 'V = I + R', 'V = I − R'], a: 0 },
      { p: '12V kaynağa bağlı 4Ω direnç. Akım kaçtır?', opts: ['2 A', '3 A', '4 A', '48 A'], a: 1, sub: '12V / 4Ω' },
      { p: 'Güç formülü?', opts: ['P = V · I', 'P = V / I', 'P = V + I', 'P = V − I'], a: 0 },
      { p: 'Yarı iletken örneği hangisidir?', opts: ['Bakır', 'Cam', 'Silisyum', 'Hava'], a: 2 },
    ],
    b4: [
      { p: '14.205 MHz hangi banttadır?', opts: ['80m', '40m', '20m', '10m'], a: 2, sub: '14.205' },
      { p: '2m bandının frekansı yaklaşık?', opts: ['7 MHz', '14 MHz', '145 MHz', '435 MHz'], a: 2 },
      { p: '70cm bandı hangisidir?', opts: ['28 MHz', '50 MHz', '144 MHz', '435 MHz'], a: 3 },
      { p: 'Dalga boyu formülü?', opts: ['λ = c · f', 'λ = c / f', 'λ = f / c', 'λ = c + f'], a: 1 },
    ],
    b5: [
      { p: '"Mesajım anlaşıldı" anlamına gelen ifade?', opts: ['Over', 'Roger', 'Wilco', 'Break'], a: 1 },
      { p: '"Anlaşıldı + uygulayacağım" anlamına gelen?', opts: ['Roger', 'Wilco', 'Out', 'Copy'], a: 1 },
      { p: 'Acil durum çağrısı hangisidir?', opts: ['CQ', 'Pan-Pan', 'MAYDAY', 'Roger'], a: 2 },
      { p: 'Türkiye prefix\'i hangisidir?', opts: ['TA', 'DL', 'F', 'G'], a: 0, sub: 'TA1ABC' },
    ],
    b6: [
      { p: 'A sınıfı lisansla en üst yetki nedir?', opts: ['Tam yetki', 'Sadece VHF', 'Sadece dinleme', 'Sadece eğitim'], a: 0 },
      { p: 'TRAC açılımı?', opts: ['Türkiye Radyo Amatörleri Cemiyeti', 'Telsiz Radyo A. C.', 'Türk Radyo Akademik C.', 'Türk Radio Amateur C.'], a: 0 },
      { p: 'Çağrı işareti yapısı nasıldır?', opts: ['Prefix + sayı + sufiks', 'Sadece harfler', 'Sadece rakamlar', 'Hiçbir yapı'], a: 0, sub: 'TA1ABC' },
      { p: 'TRAC sınavı geçme puanı?', opts: ['50%', '60%', '70%', '80%'], a: 1 },
    ],
  };
  const set = map[bolum.id] || [];
  return set.map(q => ({
    tag: `${bolum.baslik.toUpperCase()} · ÇOKTAN SEÇMELİ`,
    prompt: q.p,
    subtitle: q.sub || null,
    options: q.opts,
    correctIndex: q.a,
  }));
}

function shuffled(arr) { return [...arr].sort(() => Math.random() - 0.5); }

// ──────────────────────────────────────────────────────────────
// Sınav menüsü
// ──────────────────────────────────────────────────────────────
function Sinav({ t, state, go }) {
  const days = state.examDays;
  return (
    <div className="screen" style={{ background: t.bg }}>
      <ScreenHeader t={t} title="Sınav" eyebrow="TRAC HAZIRLIK" onBack={() => go({ screen: 'home' })} accent={t.accent}/>
      <div style={{ padding: '0 16px' }}>
        <window.Card t={t} style={{ padding: 18, position: 'relative', overflow: 'hidden' }}>
          <window.SpectrumBg t={t} accent={t.accent} />
          <div style={{ position: 'relative' }}>
            <window.Mono t={t} style={{ color: t.accent }}>T-MINUS</window.Mono>
            <div style={{ fontFamily: t.monoFont, fontWeight: 800, fontSize: 56, color: t.ink, letterSpacing: -2, lineHeight: 1 }}>
              {String(days).padStart(2,'0')}
              <span style={{ fontSize: 18, color: t.muted, marginLeft: 6 }}>gün</span>
            </div>
            <div style={{ fontSize: 13, color: t.muted, marginTop: 6 }}>
              Bir sonraki TRAC sınavına kadar
            </div>
            <div style={{ marginTop: 14, display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: 8 }}>
              <StatBox t={t} label="HAZIR" value={`${Math.round(state.examReady * 100)}%`} hilite={t.success}/>
              <StatBox t={t} label="DENEME" value={state.denemeSinav}/>
              <StatBox t={t} label="ORT.PUAN" value={`${state.ortalamaPuan}%`}/>
            </div>
          </div>
        </window.Card>

        <div style={{ marginTop: 14, display: 'grid', gap: 10 }}>
          <SinavRow t={t} title="Tam Deneme Sınavı" sub="40 soru · gerçek format · 60 dk"
            cta="Başla" tone="primary" />
          <SinavRow t={t} title="Bölüm Sınavı" sub="Tek bölüm odaklı, 15–20 soru" cta="Seç" />
          <SinavRow t={t} title="Hızlı 10" sub="Karışık 10 soru · ~5 dk" cta="Başla" />
          <SinavRow t={t} title="Çıkmış Sorular Arşivi" sub="Geçmiş sınavlardan derlenmiş set" cta="Aç" />
          <SinavRow t={t} title="Zayıf Konu Sınavı" sub={`${state.zayifKonu} konu üzerinden ${state.zayifKonu * 2} soru`} cta="Çalış" tone="warn"/>
        </div>
        <div style={{ height: 100 }} />
      </div>
    </div>
  );
}

function SinavRow({ t, title, sub, cta, tone }) {
  const bg = tone === 'primary' ? t.ink : tone === 'warn' ? t.streakTint : t.surface;
  const fg = tone === 'primary' ? t.surface : tone === 'warn' ? t.streak : t.ink;
  const sBg = tone === 'primary' ? t.surface : t.ink;
  const sFg = tone === 'primary' ? t.ink : t.surface;
  return (
    <div style={{
      display: 'flex', gap: 10, alignItems: 'center',
      padding: '14px 14px', borderRadius: t.radius,
      background: bg, border: `1px solid ${tone === 'primary' ? t.ink : t.hairline}`,
    }}>
      <div style={{ flex: 1 }}>
        <div style={{ fontWeight: 700, fontSize: 15, color: fg, letterSpacing: -0.2 }}>{title}</div>
        <div style={{ fontSize: 12.5, color: tone === 'primary' ? '#ffffffaa' : t.muted, marginTop: 2 }}>{sub}</div>
      </div>
      <button style={{
        background: sBg, color: sFg, border: 'none', padding: '8px 14px',
        borderRadius: 8, fontWeight: 700, fontSize: 12, cursor: 'pointer',
        fontFamily: t.monoFont, letterSpacing: 0.5,
      }}>{cta.toUpperCase()}</button>
    </div>
  );
}

// ──────────────────────────────────────────────────────────────
// Kütüphane — reference cards
// ──────────────────────────────────────────────────────────────
function Kutuphane({ t, go }) {
  const [tab, setTab] = u2State('nato');
  return (
    <div className="screen" style={{ background: t.bg }}>
      <ScreenHeader t={t} title="Kütüphane" eyebrow="REFERANS" onBack={() => go({ screen: 'home' })} accent={t.accent}/>

      <div style={{ padding: '0 16px 12px' }}>
        <div style={{
          display: 'flex', gap: 6, padding: 4,
          background: t.surface, border: `1px solid ${t.hairline}`,
          borderRadius: 10,
        }}>
          {[['nato','NATO Alfabesi'],['q','Q Kodları'],['morse','Morse'],['proc','Prosedür']].map(([k, lbl]) => (
            <button key={k} onClick={() => setTab(k)} style={{
              flex: 1, padding: '8px 6px', borderRadius: 7,
              background: tab === k ? t.ink : 'transparent',
              color: tab === k ? t.surface : t.muted,
              border: 'none', cursor: 'pointer',
              fontWeight: 700, fontSize: 11, fontFamily: t.monoFont, letterSpacing: 0.5,
            }}>{lbl}</button>
          ))}
        </div>
      </div>

      <div style={{ padding: '0 16px' }}>
        {tab === 'nato' && (
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 8 }}>
            {window.NATO.map(([l, w, p]) => (
              <div key={l} style={{
                padding: 12, borderRadius: 10,
                background: t.surface, border: `1px solid ${t.hairline}`,
                display: 'flex', alignItems: 'center', gap: 10,
              }}>
                <div style={{
                  width: 36, height: 36, borderRadius: 8,
                  background: t.accentTint, color: t.accent,
                  display: 'grid', placeItems: 'center',
                  fontFamily: t.monoFont, fontWeight: 800, fontSize: 18,
                }}>{l}</div>
                <div>
                  <div style={{ fontWeight: 700, fontSize: 13, color: t.ink }}>{w}</div>
                  <div style={{ fontFamily: t.monoFont, fontSize: 10, color: t.muted, letterSpacing: 0.5 }}>{p}</div>
                </div>
              </div>
            ))}
          </div>
        )}
        {tab === 'q' && (
          <div style={{ display: 'grid', gap: 8 }}>
            {window.QKODES.map(([k, v]) => (
              <div key={k} style={{
                padding: '12px 14px', borderRadius: 10,
                background: t.surface, border: `1px solid ${t.hairline}`,
                display: 'flex', alignItems: 'center', gap: 14,
              }}>
                <div style={{
                  fontFamily: t.monoFont, fontWeight: 800, fontSize: 18, color: t.accent,
                  minWidth: 56, letterSpacing: 0.5,
                }}>{k}</div>
                <div style={{ flex: 1, fontSize: 13.5, color: t.ink }}>{v}</div>
              </div>
            ))}
          </div>
        )}
        {tab === 'morse' && (
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr 1fr', gap: 8 }}>
            {window.NATO.map(([l]) => {
              const m = { A:'.-',B:'-...',C:'-.-.',D:'-..',E:'.',F:'..-.',G:'--.',H:'....',I:'..',J:'.---',K:'-.-',L:'.-..',M:'--',N:'-.',O:'---',P:'.--.',Q:'--.-',R:'.-.',S:'...',T:'-',U:'..-',V:'...-',W:'.--',X:'-..-',Y:'-.--',Z:'--..' }[l] || '';
              return (
                <div key={l} style={{
                  padding: 10, borderRadius: 8,
                  background: t.surface, border: `1px solid ${t.hairline}`,
                  textAlign: 'center',
                }}>
                  <div style={{ fontFamily: t.monoFont, fontWeight: 800, fontSize: 20, color: t.ink }}>{l}</div>
                  <div style={{ fontFamily: t.monoFont, fontSize: 12, color: t.accent, letterSpacing: 1, marginTop: 2 }}>{m}</div>
                </div>
              );
            })}
          </div>
        )}
        {tab === 'proc' && (
          <div style={{ display: 'grid', gap: 8 }}>
            {[
              ['OVER','Ben konuştum, sıra sende'],
              ['OUT','Konuşma bitti, kapatıyorum'],
              ['ROGER','Anlaşıldı'],
              ['WILCO','Anlaşıldı, uygulayacağım'],
              ['COPY','Aldım / anladım'],
              ['BREAK','Acil bir ara talep'],
              ['CQ','Genel çağrı'],
              ['MAYDAY','Hayati tehlike — acil yardım'],
              ['PAN-PAN','Acil ama hayati değil'],
            ].map(([k,v]) => (
              <div key={k} style={{
                padding: '12px 14px', borderRadius: 10,
                background: t.surface, border: `1px solid ${t.hairline}`,
              }}>
                <div style={{ fontFamily: t.monoFont, fontWeight: 800, fontSize: 14, color: t.accent, letterSpacing: 1 }}>{k}</div>
                <div style={{ fontSize: 13, color: t.ink, marginTop: 2 }}>{v}</div>
              </div>
            ))}
          </div>
        )}
        <div style={{ height: 100 }} />
      </div>
    </div>
  );
}

// ──────────────────────────────────────────────────────────────
// Profil
// ──────────────────────────────────────────────────────────────
function Profil({ t, state, go }) {
  const lvl = window.level(state.xp);
  const rozeller = window.ROZETLER;
  const kazanilan = rozeller.filter(r => r.kazanildi).length;

  return (
    <div className="screen" style={{ background: t.bg }}>
      <ScreenHeader t={t} title="Profil" eyebrow="OPERATÖR" onBack={() => go({ screen: 'home' })} accent={t.accent}/>

      <div style={{ padding: '0 16px' }}>
        <window.Card t={t} style={{ padding: 18 }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 14 }}>
            <div style={{
              width: 64, height: 64, borderRadius: 16,
              background: t.accentTint, color: t.accent,
              border: `1px solid ${t.accent}30`,
              display: 'grid', placeItems: 'center',
              fontFamily: t.monoFont, fontWeight: 800, fontSize: 20,
            }}>TA2</div>
            <div style={{ flex: 1 }}>
              <div style={{ fontFamily: t.monoFont, fontSize: 10.5, letterSpacing: 1.4, color: t.muted, fontWeight: 700 }}>
                ÇAĞRI · TA2/CALL · A SINIFI ADAYI
              </div>
              <div style={{ marginTop: 2, fontSize: 19, fontWeight: 800, color: t.ink, letterSpacing: -0.3 }}>
                Seviye {lvl.no} · {lvl.isim}
              </div>
            </div>
          </div>
          <div style={{ marginTop: 14, display: 'flex', alignItems: 'center', gap: 10 }}>
            <window.Bar t={t} pct={lvl.pct} color={t.accent} style={{ flex: 1 }} />
            <div style={{ fontFamily: t.monoFont, fontSize: 11, color: t.muted, minWidth: 64, textAlign: 'right' }}>
              {state.xp}/{lvl.next} XP
            </div>
          </div>
        </window.Card>

        <div style={{ marginTop: 14, display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: 10 }}>
          <StatBox t={t} label="STREAK" value={state.streak} hilite={t.streak} />
          <StatBox t={t} label="TAM. DERS" value={state.tamamlananDers} hilite={t.success}/>
          <StatBox t={t} label="ROZET" value={`${kazanilan}/${rozeller.length}`} hilite={t.accent}/>
        </div>

        <div style={{ marginTop: 18, marginBottom: 8 }}>
          <window.Mono t={t}>ROZETLER · {kazanilan}/{rozeller.length}</window.Mono>
        </div>
        <div style={{
          display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: 8,
        }}>
          {rozeller.map(r => (
            <div key={r.id} style={{
              padding: '12px 8px', borderRadius: t.radius,
              background: t.surface, border: `1px solid ${r.kazanildi ? t.accent : t.hairline}`,
              textAlign: 'center',
              opacity: r.kazanildi ? 1 : 0.55,
            }}>
              <div style={{
                width: 44, height: 44, borderRadius: 12, margin: '0 auto',
                background: r.kazanildi ? t.accent : t.hairline,
                color: r.kazanildi ? t.surface : t.mute2,
                display: 'grid', placeItems: 'center',
                fontFamily: t.monoFont, fontWeight: 800, fontSize: 12, letterSpacing: 0.5,
              }}>{r.glyph}</div>
              <div style={{ marginTop: 6, fontSize: 11, fontWeight: 700, color: t.ink }}>{r.isim}</div>
              <div style={{ fontFamily: t.monoFont, fontSize: 9, color: t.muted, marginTop: 2, lineHeight: 1.2 }}>
                {r.aciklama}
              </div>
            </div>
          ))}
        </div>

        <div style={{ marginTop: 18, marginBottom: 8 }}>
          <window.Mono t={t}>BÖLÜM İLERLEMESİ</window.Mono>
        </div>
        <window.Card t={t} style={{ padding: 14 }}>
          {window.CURRICULUM.map((b, i) => {
            const p = window.chapterProgress(b, state.ilerleme);
            return (
              <div key={b.id} style={{
                display: 'flex', alignItems: 'center', gap: 10,
                padding: '8px 0',
                borderBottom: i < window.CURRICULUM.length - 1 ? `1px dashed ${t.hairline}` : 'none',
              }}>
                <div style={{
                  fontFamily: t.monoFont, fontWeight: 700, fontSize: 11,
                  color: b.renk, minWidth: 38,
                }}>B{String(b.no).padStart(2,'0')}</div>
                <div style={{ flex: 1, fontSize: 13, color: t.ink, fontWeight: 600 }}>{b.baslik}</div>
                <div style={{ width: 70 }}>
                  <window.Bar t={t} pct={p.pct} color={b.renk} height={4}/>
                </div>
                <div style={{ fontFamily: t.monoFont, fontSize: 11, color: t.muted, minWidth: 36, textAlign: 'right' }}>
                  {Math.round(p.pct * 100)}%
                </div>
              </div>
            );
          })}
        </window.Card>
        <div style={{ height: 100 }} />
      </div>
    </div>
  );
}

// ──────────────────────────────────────────────────────────────
// Pratik (weak topics) — minimal
// ──────────────────────────────────────────────────────────────
function Pratik({ t, state, go }) {
  return (
    <div className="screen" style={{ background: t.bg }}>
      <ScreenHeader t={t} title="Zayıf Konu Pratiği" eyebrow="HEDEFLI ÇALIŞMA" onBack={() => go({ screen: 'home' })} accent={t.streak}/>
      <div style={{ padding: '0 16px' }}>
        <window.Card t={t} style={{ padding: 18, textAlign: 'center' }}>
          <div style={{
            width: 64, height: 64, borderRadius: 16,
            background: t.streakTint, color: t.streak,
            display: 'grid', placeItems: 'center', margin: '0 auto',
            fontFamily: t.monoFont, fontWeight: 800, fontSize: 24,
          }}>×{state.zayifKonu}</div>
          <div style={{ marginTop: 10, fontWeight: 800, fontSize: 18, color: t.ink }}>
            {state.zayifKonu} konu seni bekliyor
          </div>
          <div style={{ fontSize: 13, color: t.muted, marginTop: 4 }}>
            Daha önce zorlandığın sorulardan derlenmiş, kısa bir pratik turu.
          </div>
          <button onClick={() => go({ screen: 'home' })} style={{
            marginTop: 16, width: '100%', padding: 14,
            background: t.streak, color: '#fff', border: 'none',
            borderRadius: 12, fontWeight: 700, fontSize: 15, cursor: 'pointer',
          }}>Pratiği başlat</button>
        </window.Card>
      </div>
    </div>
  );
}

// ──────────────────────────────────────────────────────────────
// Shared header
// ──────────────────────────────────────────────────────────────
function ScreenHeader({ t, title, eyebrow, onBack, accent }) {
  return (
    <div style={{ padding: '14px 16px 14px' }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
        <button onClick={onBack} style={{
          width: 36, height: 36, borderRadius: 10,
          background: t.surface, border: `1px solid ${t.hairline}`,
          color: t.ink, cursor: 'pointer', fontSize: 16,
          display: 'grid', placeItems: 'center',
        }}>←</button>
        <div>
          <div style={{ fontFamily: t.monoFont, fontSize: 10, letterSpacing: 1.6, color: accent, fontWeight: 700 }}>
            {eyebrow}
          </div>
          <div style={{ fontSize: 22, fontWeight: 800, color: t.ink, letterSpacing: -0.4, lineHeight: 1.1, marginTop: 1 }}>
            {title}
          </div>
        </div>
      </div>
    </div>
  );
}

window.BolumDetay = BolumDetay;
window.Ders = Ders;
window.Sinav = Sinav;
window.Kutuphane = Kutuphane;
window.Profil = Profil;
window.Pratik = Pratik;
