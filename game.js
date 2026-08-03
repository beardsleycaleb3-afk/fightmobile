/**
 * FightMobile Game Logic & Touch-Only Portrait Arcade Controller (game.js)
 * Designed strictly for Portrait-Only, Touch-Only Android Chrome Mobile
 */

// REGISTER SERVICE WORKER FOR PWA / WEBVIEW SUPPORT
if ('serviceWorker' in navigator) {
  window.addEventListener('load', function() {
    navigator.serviceWorker.register('SW.js').catch(function() {});
  });
}

// ARCADE STATE & LORE CODEX
var arcadeScore = 12450;
var bossesDefeated = 3;
var collectablesFound = 18;
var arcadeRank = "S-Rank Celestial Voyager";
var latestLore = "Codex #04: Titan of the Astral Rift fractured into shimmering stardust.";
var comboCount = 1;
var lastHitTime = 0;

var nextId = 1;
var hero = null;

// TOUCH JOYSTICK & ACTION BUTTONS (PORTRAIT ONLY, TOUCH ONLY)
var touchState = {
  moveX: 0,
  moveY: 0,
  jumpPressed: false,
  burstPressed: false,
  activeTouchId: null,
  baseX: 0,
  baseY: 0
};

function initTouchControls() {
  var joystickBase = document.getElementById('joystick-base');
  var joystickThumb = document.getElementById('joystick-thumb');
  var btnJump = document.getElementById('btn-jump');
  var btnBurst = document.getElementById('btn-burst');

  if (!joystickBase || !joystickThumb) return;

  joystickBase.addEventListener('touchstart', function(e) {
    e.preventDefault();
    var touch = e.changedTouches[0];
    touchState.activeTouchId = touch.identifier;
    var rect = joystickBase.getBoundingClientRect();
    touchState.baseX = rect.left + rect.width / 2;
    touchState.baseY = rect.top + rect.height / 2;
    updateJoystickThumb(touch.clientX, touch.clientY);
  }, { passive: false });

  window.addEventListener('touchmove', function(e) {
    if (touchState.activeTouchId === null) return;
    for (var i = 0; i < e.changedTouches.length; i++) {
      var touch = e.changedTouches[i];
      if (touch.identifier === touchState.activeTouchId) {
        e.preventDefault();
        updateJoystickThumb(touch.clientX, touch.clientY);
        break;
      }
    }
  }, { passive: false });

  var resetJoystick = function(e) {
    for (var i = 0; i < e.changedTouches.length; i++) {
      var touch = e.changedTouches[i];
      if (touch.identifier === touchState.activeTouchId) {
        touchState.activeTouchId = null;
        touchState.moveX = 0;
        touchState.moveY = 0;
        if (joystickThumb) {
          joystickThumb.style.transform = 'translate(0px, 0px)';
        }
        break;
      }
    }
  };
  window.addEventListener('touchend', resetJoystick, { passive: false });
  window.addEventListener('touchcancel', resetJoystick, { passive: false });

  function updateJoystickThumb(tx, ty) {
    var dx = tx - touchState.baseX;
    var dy = ty - touchState.baseY;
    var dist = Math.sqrt(dx * dx + dy * dy);
    var maxDist = 45;
    if (dist > maxDist) {
      dx = (dx / dist) * maxDist;
      dy = (dy / dist) * maxDist;
    }
    touchState.moveX = dx / maxDist;
    touchState.moveY = dy / maxDist;
    if (joystickThumb) {
      joystickThumb.style.transform = 'translate(' + dx + 'px, ' + dy + 'px)';
    }
  }

  // Action Buttons (Touch Only)
  if (btnJump) {
    btnJump.addEventListener('touchstart', function(e) {
      e.preventDefault();
      touchState.jumpPressed = true;
      if (hero && hero.onGround) {
        hero.vy = -14;
        hero.onGround = false;
        createSparks(hero.x, hero.y + hero.h/2, '#D0BCFF', 10, 3);
        playSound(520, 0.12, 'square', 0.18);
      }
    }, { passive: false });
    btnJump.addEventListener('touchend', function(e) {
      e.preventDefault();
      touchState.jumpPressed = false;
    }, { passive: false });
  }

  if (btnBurst) {
    btnBurst.addEventListener('touchstart', function(e) {
      e.preventDefault();
      touchState.burstPressed = true;
      if (hero) {
        createShockwave(hero.x, hero.y, '#FFE37A');
        createSparks(hero.x, hero.y, '#FFE37A', 20, 8);
        playSound(220, 0.2, 'sawtooth', 0.22);
        // Apply radial burst to surrounding bodies
        for (var i = 0; i < bodies.length; i++) {
          var b = bodies[i];
          if (b !== hero && !b.isStatic) {
            var dx = b.x - hero.x;
            var dy = b.y - hero.y;
            var dist = Math.sqrt(dx * dx + dy * dy) || 1;
            if (dist < 180) {
              var force = (180 - dist) * 0.18;
              b.vx += (dx / dist) * force;
              b.vy += (dy / dist) * force;
            }
          }
        }
      }
    }, { passive: false });
    btnBurst.addEventListener('touchend', function(e) {
      e.preventDefault();
      touchState.burstPressed = false;
    }, { passive: false });
  }
}

// ARCADE HUD UPDATER
function notifyArcadeUpdate(scoreAdd, newLore, isHit) {
  var now = Date.now();
  if (isHit) {
    if (now - lastHitTime < 1800) {
      comboCount++;
      scoreAdd = (scoreAdd || 100) * comboCount;
    } else {
      comboCount = 1;
    }
    lastHitTime = now;
    var elCombo = document.getElementById('combo-hud');
    if (elCombo) {
      elCombo.textContent = 'COMBO x' + comboCount;
      elCombo.style.color = comboCount > 2 ? '#FFE37A' : '#FF8A00';
    }
  }
  if (scoreAdd) arcadeScore += scoreAdd;
  if (newLore) latestLore = newLore;
  if (arcadeScore > 25000) {
    arcadeRank = "Mythic Astral Conqueror";
  } else if (arcadeScore > 15000) {
    arcadeRank = "S-Rank Celestial Voyager";
  }

  var elScore = document.getElementById('arcade-score-val');
  if (elScore) elScore.textContent = arcadeScore;
  var elBoss = document.getElementById('arcade-boss-val');
  if (elBoss) elBoss.textContent = bossesDefeated;
  var elColl = document.getElementById('arcade-coll-val');
  if (elColl) elColl.textContent = collectablesFound;
  var elRank = document.getElementById('arcade-rank-val');
  if (elRank) elRank.textContent = arcadeRank;
  var elLore = document.getElementById('arcade-lore-text');
  if (elLore) elLore.textContent = latestLore;

  if (window.AndroidBridge && window.AndroidBridge.onArcadeScore) {
    window.AndroidBridge.onArcadeScore(arcadeScore, bossesDefeated, collectablesFound, arcadeRank, latestLore);
  }
}

function launchFightMobileUrl() {
  if (window.AndroidBridge && window.AndroidBridge.onLaunchFightMobile) {
    window.AndroidBridge.onLaunchFightMobile();
  } else {
    window.location.href = 'https://beardsleycaleb3-afk.github.io/fightmobile/';
  }
}

// SPAWN ARCADE OBJECTS
function spawnObject(type, x, y) {
  var body = null;
  if (type === 'hero') {
    body = new PhysicsBody({
      name: 'Wanderer Hero',
      type: 'hero',
      x: x, y: y,
      w: 36, h: 48,
      mass: 8,
      restitution: 0.2,
      color: '#D0BCFF',
      borderColor: '#381E72',
      isHero: true
    });
    hero = body;
  } else if (type === 'crate') {
    body = new PhysicsBody({
      name: 'Wooden Crate #' + nextId,
      type: 'box',
      x: x, y: y,
      w: 38, h: 38,
      mass: 12,
      restitution: 0.35,
      color: '#8D6E63',
      borderColor: '#5D4037'
    });
  } else if (type === 'sphere') {
    body = new PhysicsBody({
      name: 'Steel Sphere #' + nextId,
      type: 'sphere',
      x: x, y: y,
      r: 20,
      mass: 18,
      restitution: 0.75,
      color: '#90A4AE',
      borderColor: '#455A64'
    });
  } else if (type === 'diamond') {
    body = new PhysicsBody({
      name: 'Bouncy Diamond #' + nextId,
      type: 'diamond',
      x: x, y: y,
      w: 34, h: 34,
      r: 20,
      mass: 10,
      restitution: 0.92,
      color: '#7AD9FF',
      borderColor: '#004D40'
    });
  } else if (type === 'collectable') {
    body = new PhysicsBody({
      name: 'Astral Prism #' + nextId,
      type: 'collectable',
      x: x, y: y,
      w: 24, h: 24,
      r: 16,
      mass: 0,
      isStatic: true,
      restitution: 0.9,
      color: '#7AD9FF',
      borderColor: '#EADDFF'
    });
  } else if (type === 'breakable') {
    body = new PhysicsBody({
      name: 'Monolith #' + nextId,
      type: 'breakable',
      x: x, y: y,
      w: 32, h: 48,
      mass: 14,
      restitution: 0.3,
      color: '#D0BCFF',
      borderColor: '#6750A4'
    });
  } else if (type === 'enemy') {
    body = new PhysicsBody({
      name: 'Shadow Sentinel #' + nextId,
      type: 'enemy',
      x: x, y: y,
      r: 18,
      mass: 16,
      restitution: 0.5,
      color: '#B3261E',
      borderColor: '#F2B8B5'
    });
    body.patrolBaseX = x;
  } else if (type === 'boss') {
    body = new PhysicsBody({
      name: 'Titan of Astral Rift',
      type: 'boss',
      x: x, y: y,
      w: 48, h: 48,
      r: 26,
      mass: 0,
      isStatic: true,
      restitution: 0.85,
      color: '#6750A4',
      borderColor: '#FFE37A',
      shieldHP: 3
    });
  }

  if (body) {
    nextId++;
    bodies.push(body);
  }
  return body;
}

function initDefaultScene() {
  bodies = [];
  nextId = 1;

  // Hero in center portrait
  spawnObject('hero', W * 0.5, H * 0.4);

  // Wooden Crates
  spawnObject('crate', W * 0.35, H * 0.25);
  spawnObject('crate', W * 0.65, H * 0.18);

  // Steel Sphere & Bouncy Diamond
  spawnObject('sphere', W * 0.6, H * 0.35);
  spawnObject('diamond', W * 0.75, H * 0.22);

  // Arcade Boss: Titan of the Astral Rift
  spawnObject('boss', W * 0.5, H * 0.18);

  // Arcade Enemy: Shadow Sentinel
  spawnObject('enemy', W * 0.3, H * 0.45);

  // Arcade Collectables: Astral Memory Prisms
  spawnObject('collectable', W * 0.25, H * 0.32);
  spawnObject('collectable', W * 0.78, H * 0.35);

  // Arcade Breakables: Crystal Monoliths
  spawnObject('breakable', W * 0.42, H * 0.2);
  spawnObject('breakable', W * 0.68, H * 0.26);
}

// ARCADE OBJECT HIT HANDLER
function handleArcadeHit(target, hitter, impulseScalar) {
  if (!target) return;
  if (target.type === 'collectable') {
    createSparks(target.x, target.y, '#FFE37A', 25, 12);
    createSparks(target.x, target.y, '#7AD9FF', 18, 10);
    createArtisticFlairTrail(target.x, target.y);
    collectablesFound++;
    notifyArcadeUpdate(500, "Codex #" + collectablesFound + ": Astral Prism collected. Light crystallizes into ancient memory.", true);
    playSound(880, 0.15, 'sine', 0.15);
    var idx = bodies.indexOf(target);
    if (idx > -1) bodies.splice(idx, 1);
  } else if (target.type === 'breakable' && Math.abs(impulseScalar) > 1.0) {
    createSparks(target.x, target.y, '#E8DEF8', 30, 15);
    createArtisticFlairTrail(target.x, target.y);
    notifyArcadeUpdate(300, "Codex Monolith: Crystal shattered. Residual kinetic energy radiates through the rift.", true);
    playSound(320, 0.18, 'sawtooth', 0.15);
    var idx2 = bodies.indexOf(target);
    if (idx2 > -1) bodies.splice(idx2, 1);
  } else if (target.type === 'enemy' && (hitter.isHero || Math.abs(impulseScalar) > 1.5)) {
    createSparks(target.x, target.y, '#FFE37A', 35, 16);
    createSparks(target.x, target.y, '#FF8A00', 20, 12);
    createArtisticFlairTrail(target.x, target.y);
    notifyArcadeUpdate(1200, "Codex Sentinel: Shadow guardian vanquished. The celestial path clears.", true);
    playSound(240, 0.22, 'square', 0.18);
    var idx3 = bodies.indexOf(target);
    if (idx3 > -1) bodies.splice(idx3, 1);
  } else if (target.type === 'boss') {
    target.shieldHP = (target.shieldHP || 3) - 1;
    createSparks(target.x, target.y, '#6750A4', 35, 18);
    createSparks(target.x, target.y, '#EADDFF', 25, 14);
    createArtisticFlairTrail(target.x, target.y);
    playSound(180, 0.25, 'sawtooth', 0.22);
    if (target.shieldHP <= 0) {
      bossesDefeated++;
      notifyArcadeUpdate(5000, "Codex Boss: TITAN OF THE ASTRAL RIFT DEFEATED! The celestial nexus rings with triumph!", true);
      var idx4 = bodies.indexOf(target);
      if (idx4 > -1) bodies.splice(idx4, 1);
      setTimeout(function() {
        spawnObject('boss', W * 0.5, H * 0.18);
      }, 10000);
    } else {
      notifyArcadeUpdate(1500, "Codex Boss: Titan shield weakened (" + target.shieldHP + "/3 HP remaining)!", true);
    }
  }
}

// SIMULATION & RENDER LOOP
var lastTime = 0;
var fpsCounter = 0;
var fpsTimer = 0;

function loop(timestamp) {
  var dt = (timestamp - lastTime) / 1000 || 0.016;
  lastTime = timestamp;
  if (dt > 0.1) dt = 0.016;

  fpsCounter++;
  fpsTimer += dt;
  if (fpsTimer >= 1.0) {
    if (window.AndroidBridge && window.AndroidBridge.onFpsUpdate) {
      window.AndroidBridge.onFpsUpdate(fpsCounter, bodies.length);
    }
    fpsCounter = 0;
    fpsTimer = 0;
  }

  // Handle Hero Touch Controller Input
  if (hero) {
    if (Math.abs(touchState.moveX) > 0.05) {
      hero.vx += touchState.moveX * 0.8;
    }
  }

  // Clear canvas
  ctx.clearRect(0, 0, W, H);

  // Grid background
  drawArtisticGrid();

  // Physics sub-stepping
  var steps = 3;
  clearSpatialHash();
  for (var i = 0; i < bodies.length; i++) {
    insertToHash(bodies[i]);
  }

  for (var s = 0; s < steps; s++) {
    for (var i = 0; i < bodies.length; i++) {
      var b = bodies[i];
      if (b.type === 'enemy' && b.patrolBaseX) {
        b.x = b.patrolBaseX + Math.sin(Date.now() * 0.002) * 55;
        b.rot += 0.03;
      }
      if (b.isStatic || b.isDragging) continue;

      b.vx += gravity.x / steps;
      b.vy += (gravity.y * 0.08) / steps;
      b.vx *= Math.pow(airResistance, 1/steps);
      b.vy *= Math.pow(airResistance, 1/steps);

      b.x += b.vx;
      b.y += b.vy;
      b.rot += b.vRot;

      // Floor & Wall Collisions (Portrait Bounds)
      var margin = 16;
      if (b.y + b.h/2 > H - margin) {
        b.y = H - margin - b.h/2;
        b.vy = -b.vy * b.restitution;
        b.vx *= 0.85;
        b.onGround = true;
      } else {
        b.onGround = false;
      }
      if (b.y - b.h/2 < margin) {
        b.y = margin + b.h/2;
        b.vy = -b.vy * b.restitution;
      }
      if (b.x - b.w/2 < margin) {
        b.x = margin + b.w/2;
        b.vx = -b.vx * b.restitution;
      }
      if (b.x + b.w/2 > W - margin) {
        b.x = W - margin - b.w/2;
        b.vx = -b.vx * b.restitution;
      }

      // Check Spatial Hash Collisions
      var cellX = Math.floor(b.x / cellSize);
      var cellY = Math.floor(b.y / cellSize);
      for (var cx = cellX - 1; cx <= cellX + 1; cx++) {
        for (var cy = cellY - 1; cy <= cellY + 1; cy++) {
          var key = cx + ',' + cy;
          var neighbors = spatialHash[key];
          if (!neighbors) continue;
          for (var n = 0; n < neighbors.length; n++) {
            var o = neighbors[n];
            if (b !== o) {
              resolveCollision(b, o, function(b1, b2, impulseScalar, nx, ny) {
                handleArcadeHit(b1, b2, impulseScalar);
                handleArcadeHit(b2, b1, impulseScalar);
              });
            }
          }
        }
      }
    }
  }

  // Draw Bodies
  for (var i = 0; i < bodies.length; i++) {
    bodies[i].draw(ctx);
  }

  // Draw Shockwaves
  for (var sw = shockwaves.length - 1; sw >= 0; sw--) {
    var wave = shockwaves[sw];
    wave.r += 3;
    wave.alpha -= 0.025;
    if (wave.alpha <= 0 || wave.r >= wave.maxR) {
      shockwaves.splice(sw, 1);
      continue;
    }
    ctx.save();
    ctx.beginPath();
    ctx.arc(wave.x, wave.y, wave.r, 0, Math.PI * 2);
    ctx.strokeStyle = wave.color;
    ctx.lineWidth = 3 * wave.alpha;
    ctx.globalAlpha = wave.alpha;
    ctx.stroke();
    ctx.restore();
  }

  // Draw Particles
  for (var p = particles.length - 1; p >= 0; p--) {
    var pt = particles[p];
    pt.x += pt.vx;
    pt.y += pt.vy;
    pt.life--;
    if (pt.life <= 0) {
      particles.splice(p, 1);
      continue;
    }
    ctx.save();
    ctx.globalAlpha = pt.life / pt.maxLife;
    ctx.fillStyle = pt.color;
    ctx.beginPath();
    ctx.arc(pt.x, pt.y, pt.size, 0, Math.PI * 2);
    ctx.fill();
    ctx.restore();
  }

  requestAnimationFrame(loop);
}

function drawArtisticGrid() {
  ctx.save();
  ctx.strokeStyle = 'rgba(234, 221, 255, 0.04)';
  ctx.lineWidth = 1;
  var step = 40;
  for (var x = 0; x <= W; x += step) {
    ctx.beginPath();
    ctx.moveTo(x, 0);
    ctx.lineTo(x, H);
    ctx.stroke();
  }
  for (var y = 0; y <= H; y += step) {
    ctx.beginPath();
    ctx.moveTo(0, y);
    ctx.lineTo(W, y);
    ctx.stroke();
  }
  ctx.restore();
}

// EXPORT TO WINDOW FOR ANDROID BRIDGE CALLS
window.setEngineMode = function(mode) {
  currentMode = mode;
  var badge = document.getElementById('engine-mode-badge');
  if (badge) {
    badge.textContent = 'MODE: ' + mode.toUpperCase();
  }
};

window.setGravity = function(val) {
  gravity.y = parseFloat(val);
  var badge = document.getElementById('gravity-badge');
  if (badge) {
    badge.textContent = 'GRAVITY: ' + gravity.y + ' m/s²';
  }
};

window.setAirResistance = function(val) {
  airResistance = parseFloat(val);
};

window.triggerShockwaveAt = function(x, y) {
  createShockwave(x, y, '#FFE37A');
  createSparks(x, y, '#FFE37A', 25, 8);
  playSound(220, 0.2, 'sawtooth', 0.25);
};

window.resetScene = function() {
  initDefaultScene();
};

window.addEventListener('resize', function() {
  W = window.innerWidth;
  H = window.innerHeight;
  if (canvas) {
    canvas.width = W;
    canvas.height = H;
  }
});

// INITIALIZE ON DOM LOAD
window.addEventListener('DOMContentLoaded', function() {
  var canvasEl = document.getElementById('scene-canvas');
  if (canvasEl) {
    initEngine(canvasEl);
    initTouchControls();
    initDefaultScene();
    notifyArcadeUpdate(0, null, false);
    requestAnimationFrame(loop);
  }
});
