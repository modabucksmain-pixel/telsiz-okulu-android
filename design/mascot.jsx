// Vız — handheld-radio mascot. Pure SVG, animated via CSS + state.
// Moods: 'idle' | 'tuning' | 'transmit' | 'cheer' | 'think' | 'wink'
const { useState: mState, useEffect: mEffect, useRef: mRef } = React;

function Viz({ t, accent = '#1E3A8A', mood = 'idle', size = 56, talking = false }) {
  // Auto blink
  const [blink, setBlink] = mState(false);
  mEffect(() => {
    let cancelled = false;
    const loop = () => {
      if (cancelled) return;
      const next = 2400 + Math.random() * 3000;
      setTimeout(() => {
        if (cancelled) return;
        setBlink(true);
        setTimeout(() => { if (!cancelled) setBlink(false); loop(); }, 130);
      }, next);
    };
    loop();
    return () => { cancelled = true; };
  }, []);

  // Eyes follow mood
  const eyesClosed = blink || mood === 'cheer';
  const happy = mood === 'cheer' || mood === 'transmit';
  const wink = mood === 'wink';

  // Mouth path by mood
  const mouthByMood = {
    idle: 'M44 78 Q50 81 56 78',
    tuning: 'M45 79 Q50 78 55 79',
    transmit: 'M44 77 Q50 83 56 77',
    cheer: 'M42 76 Q50 86 58 76',
    think: 'M44 80 Q50 78 56 80',
    wink: 'M44 78 Q50 81 56 78',
  };

  const bodyAnim =
    mood === 'cheer' ? 'viz-cheer 0.6s ease-out' :
    mood === 'transmit' ? 'viz-shake 0.5s ease-in-out' :
    mood === 'tuning' ? 'viz-tune 0.4s ease-in-out' :
    'viz-bob 4s ease-in-out infinite';

  const antennaAnim = mood === 'transmit'
    ? 'viz-ant-shake 0.4s ease-in-out infinite'
    : 'viz-ant-sway 6s ease-in-out infinite';

  return (
    <div style={{ width: size, height: size * 1.3, position: 'relative', flexShrink: 0 }}>
      <svg viewBox="0 0 100 130" width={size} height={size * 1.3} style={{ overflow: 'visible' }}>
        <defs>
          <linearGradient id="viz-body" x1="0" y1="0" x2="0" y2="1">
            <stop offset="0%" stopColor="#3a3a3a"/>
            <stop offset="100%" stopColor="#1c1c1c"/>
          </linearGradient>
          <linearGradient id="viz-screen" x1="0" y1="0" x2="0" y2="1">
            <stop offset="0%" stopColor={accent}/>
            <stop offset="100%" stopColor={accent} stopOpacity="0.75"/>
          </linearGradient>
          <filter id="viz-glow" x="-50%" y="-50%" width="200%" height="200%">
            <feGaussianBlur stdDeviation="1.6"/>
          </filter>
        </defs>

        {/* Transmit waves emitted from antenna tip */}
        {mood === 'transmit' && (
          <g style={{ animation: 'viz-emit 0.9s ease-out infinite' }}>
            <circle cx="50" cy="14" r="6" fill="none" stroke={accent} strokeWidth="1.6" opacity="0.7"/>
            <circle cx="50" cy="14" r="11" fill="none" stroke={accent} strokeWidth="1.2" opacity="0.4"/>
            <circle cx="50" cy="14" r="16" fill="none" stroke={accent} strokeWidth="0.8" opacity="0.2"/>
          </g>
        )}

        {/* Body group with bob */}
        <g style={{ animation: bodyAnim, transformOrigin: '50px 100px' }}>
          {/* Antenna */}
          <g style={{ animation: antennaAnim, transformOrigin: '50px 42px' }}>
            <line x1="50" y1="42" x2="50" y2="16" stroke="#1c1c1c" strokeWidth="2.2" strokeLinecap="round"/>
            <circle cx="50" cy="14" r="3" fill={accent}/>
            {mood === 'transmit' && (
              <circle cx="50" cy="14" r="3" fill={accent} filter="url(#viz-glow)" opacity="0.8"/>
            )}
          </g>

          {/* Shadow */}
          <ellipse cx="50" cy="116" rx="22" ry="3" fill="#000" opacity="0.12"/>

          {/* Body */}
          <rect x="24" y="40" width="52" height="74" rx="9" fill="url(#viz-body)"/>
          {/* Body highlight strip */}
          <rect x="26" y="42" width="48" height="2" rx="1" fill="#fff" opacity="0.06"/>

          {/* Screen */}
          <rect x="30" y="50" width="40" height="32" rx="4" fill="url(#viz-screen)"/>
          {/* Screen scanlines */}
          <g opacity="0.18">
            {[54, 58, 62, 66, 70, 74, 78].map(y =>
              <line key={y} x1="30" y1={y} x2="70" y2={y} stroke="#fff" strokeWidth="0.4"/>
            )}
          </g>

          {/* Eyes */}
          {eyesClosed ? (
            <g stroke="#fff" strokeWidth="2" strokeLinecap="round" fill="none">
              <path d={happy ? 'M40 61 Q43 58 46 61' : 'M40 62 L46 62'}/>
              <path d={happy ? 'M54 61 Q57 58 60 61' : 'M54 62 L60 62'}/>
            </g>
          ) : wink ? (
            <g>
              <circle cx="43" cy="62" r="2.4" fill="#fff"/>
              <path d="M54 62 L60 62" stroke="#fff" strokeWidth="2" strokeLinecap="round" fill="none"/>
            </g>
          ) : (
            <g>
              <circle cx="43" cy="62" r="2.4" fill="#fff"/>
              <circle cx="57" cy="62" r="2.4" fill="#fff"/>
              {/* tiny shine */}
              <circle cx="43.7" cy="61.3" r="0.7" fill="#fff" opacity="0.9"/>
              <circle cx="57.7" cy="61.3" r="0.7" fill="#fff" opacity="0.9"/>
            </g>
          )}

          {/* Mouth */}
          <path d={mouthByMood[mood] || mouthByMood.idle}
                stroke="#fff" strokeWidth="1.6" strokeLinecap="round" fill="none"/>

          {/* Speaker grill (dots in 3 rows) */}
          <g fill="#3a3a3a">
            {[88, 94, 100].map(y =>
              [34, 40, 46, 52, 58, 64].map(x =>
                <circle key={`${x}-${y}`} cx={x} cy={y} r="1.4" />
              )
            )}
          </g>

          {/* PTT button on side */}
          <rect x="22" y="62" width="3" height="10" rx="1" fill={mood === 'transmit' ? accent : '#444'}/>

          {/* Knob top-right */}
          <circle cx="68" cy="45" r="2.5" fill="#444"/>
          <circle cx="68" cy="45" r="1" fill="#666"/>

          {/* TX indicator LED */}
          <circle cx="34" cy="46" r="1.6"
            fill={mood === 'transmit' ? '#ef4444' : '#2a2a2a'}/>
          {mood === 'transmit' && (
            <circle cx="34" cy="46" r="1.6" fill="#ef4444" filter="url(#viz-glow)"/>
          )}

          {/* Talking bars (sound bars on screen if talking) */}
          {talking && (
            <g style={{ animation: 'viz-talk 0.6s ease-in-out infinite' }}>
              <rect x="48" y="73" width="1.5" height="4" fill="#fff" opacity="0.7"/>
              <rect x="51" y="71" width="1.5" height="6" fill="#fff" opacity="0.85"/>
              <rect x="54" y="74" width="1.5" height="3" fill="#fff" opacity="0.6"/>
            </g>
          )}
        </g>
      </svg>
    </div>
  );
}

// Speech bubble pointing left → maskot is on left of bubble
function VizBubble({ t, children, accent }) {
  const bg = t.bg;
  const border = (accent || t.accent) + '55';
  return (
    <div style={{
      position: 'relative',
      background: bg,
      border: `1.5px solid ${border}`,
      borderRadius: 12,
      padding: '8px 12px',
      fontSize: 12.5,
      fontWeight: 600,
      color: t.ink,
      lineHeight: 1.35,
      boxShadow: `0 4px 14px ${(accent || t.accent)}22, 0 1px 0 ${(accent || t.accent)}30`,
      maxWidth: 240,
      display: 'inline-block',
    }}>
      {/* Tail */}
      <div style={{
        position: 'absolute', left: -7, top: 12,
        width: 10, height: 10, transform: 'rotate(45deg)',
        background: bg,
        borderLeft: `1.5px solid ${border}`,
        borderBottom: `1.5px solid ${border}`,
      }}/>
      {children}
    </div>
  );
}

// Inject mascot animations
if (typeof document !== 'undefined' && !document.getElementById('viz-anim')) {
  const s = document.createElement('style');
  s.id = 'viz-anim';
  s.textContent = `
    @keyframes viz-bob {
      0%, 100% { transform: translateY(0); }
      50%      { transform: translateY(-2px); }
    }
    @keyframes viz-cheer {
      0%   { transform: translateY(0) rotate(0); }
      30%  { transform: translateY(-12px) rotate(-4deg); }
      60%  { transform: translateY(-4px)  rotate(3deg); }
      100% { transform: translateY(0)     rotate(0); }
    }
    @keyframes viz-shake {
      0%, 100% { transform: translateX(0); }
      25%      { transform: translateX(-1.5px); }
      75%      { transform: translateX(1.5px); }
    }
    @keyframes viz-tune {
      0%   { transform: rotate(-3deg); }
      50%  { transform: rotate(3deg); }
      100% { transform: rotate(0); }
    }
    @keyframes viz-ant-sway {
      0%, 100% { transform: rotate(-2deg); }
      50%      { transform: rotate(2deg); }
    }
    @keyframes viz-ant-shake {
      0%, 100% { transform: rotate(-4deg); }
      50%      { transform: rotate(4deg); }
    }
    @keyframes viz-emit {
      0%   { opacity: 1;   transform: scale(0.4); }
      100% { opacity: 0;   transform: scale(1.4); }
    }
    @keyframes viz-talk {
      0%, 100% { transform: scaleY(1);   transform-origin: center 77px; }
      50%      { transform: scaleY(0.4); transform-origin: center 77px; }
    }
    @keyframes viz-bubble-in {
      0%   { opacity: 0; transform: translateY(-4px) scale(0.9); }
      100% { opacity: 1; transform: translateY(0)     scale(1);   }
    }
    @keyframes viz-fade-in {
      0%   { opacity: 0; transform: translateY(2px); }
      100% { opacity: 1; transform: translateY(0); }
    }
    @keyframes spectrum-wave {
      0%, 100% { transform: scaleY(0.6); }
      50%      { transform: scaleY(1.4); }
    }
    @keyframes transmit-ripple {
      0%   { opacity: 0.55; transform: translate(-50%, -50%) scale(0.2); }
      100% { opacity: 0;    transform: translate(-50%, -50%) scale(2.5); }
    }
    @keyframes dial-glow {
      0%, 100% { box-shadow: inset 0 0 0 0 transparent; }
      50%      { box-shadow: inset 0 0 18px 0 var(--glow); }
    }
    @keyframes chip-count {
      0% { transform: translateY(6px); opacity: 0; }
      100% { transform: translateY(0); opacity: 1; }
    }
    @keyframes ding-in {
      0%   { transform: translate(-50%, 8px) scale(0.7); opacity: 0; }
      40%  { transform: translate(-50%, -2px) scale(1.05); opacity: 1; }
      100% { transform: translate(-50%, 0) scale(1); opacity: 1; }
    }
  `;
  document.head.appendChild(s);
}

Object.assign(window, { Viz, VizBubble });
