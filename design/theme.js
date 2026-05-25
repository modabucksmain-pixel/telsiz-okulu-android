// Theme tokens — light, engineering aesthetic
// Variants: 'sade' (clean), 'spektrum' (instrument panel), 'saha' (blueprint paper)
window.makeTheme = function(variant, accent) {
  const accents = {
    blue:  { hi: '#1E3A8A', hi2: '#3151B0', tint: '#E8ECF7' },
    teal:  { hi: '#0F766E', hi2: '#14998F', tint: '#E0F0EE' },
    amber: { hi: '#92400E', hi2: '#B45309', tint: '#FBEFD9' },
  };
  const a = accents[accent] || accents.blue;

  const base = {
    accent: a.hi, accentHi: a.hi2, accentTint: a.tint,
    streak: '#DC6803', streakTint: '#FEF0DA',
    xp: '#1E3A8A',
    success: '#15803D', danger: '#B91C1C', warn: '#A16207',
    ink:    '#0B1020',
    ink2:   '#1F2937',
    muted:  '#5B6472',
    mute2:  '#8A93A1',
    hairline: '#E2DFD5',
    hairline2: '#D6D2C5',
    bg:     '#F5F2E9',  // warm paper
    surface:'#FFFFFF',
    surface2:'#FBF8F0',
    sansFont: "'Inter', 'Helvetica Neue', system-ui, sans-serif",
    monoFont: "'IBM Plex Mono', 'JetBrains Mono', ui-monospace, monospace",
    radius: 14,
    radiusSm: 8,
  };

  if (variant === 'spektrum') {
    return { ...base,
      bg: '#0B1020', ink: '#F5F2E9', ink2: '#D6D2C5',
      muted: '#9CA3AF', mute2: '#6B7280',
      hairline: '#1F2937', hairline2: '#2C3849',
      surface: '#11172A', surface2: '#0E1426',
      accent: '#5BD9C8', accentHi: '#7CEEDC',
      accentTint: 'rgba(91,217,200,0.12)',
      streak: '#FFB454', streakTint: 'rgba(255,180,84,0.14)',
      xp: '#5BD9C8',
    };
  }
  if (variant === 'saha') {
    return { ...base,
      bg: '#EDE7D6', surface: '#FFFEF7', surface2: '#F4EFE0',
      hairline: '#CFC8B1', hairline2: '#B8B097',
      ink: '#1F2435',
    };
  }
  return base;
};
