/**
 * FightMobile Core Creative Engine (engine.js)
 * High-performance 2D Rigid-Body Physics, Canvas Rendering,
 * Spatial Hash, Audio Synthesizer, & Multi-Engine Simulation Mode
 */

var W = window.innerWidth;
var H = window.innerHeight;
var canvas = null;
var ctx = null;

var bodies = [];
var particles = [];
var shockwaves = [];
var gravity = { x: 0, y: 9.8 };
var airResistance = 0.99;
var currentMode = 'phaser';

var spriteAssetBase = 'assets/sprites/player1/east';
var spriteImageCache = {};
var heroSpriteAnimations = {
  idle: ['idle/frame_000.png', 'idle/frame_001.png', 'idle/frame_002.png', 'idle/frame_003.png', 'idle/frame_004.png', 'idle/frame_005.png', 'idle/frame_006.png', 'idle/frame_007.png'],
  running: ['running/frame_000.png', 'running/frame_001.png', 'running/frame_002.png', 'running/frame_003.png', 'running/frame_004.png', 'running/frame_005.png', 'running/frame_006.png', 'running/frame_007.png'],
  falling: ['falling/frame_000.png', 'falling/frame_001.png', 'falling/frame_002.png', 'falling/frame_003.png', 'falling/frame_004.png', 'falling/frame_005.png', 'falling/frame_006.png'],
  crouch: ['crouch/frame_000.png', 'crouch/frame_001.png', 'crouch/frame_002.png', 'crouch/frame_003.png', 'crouch/frame_004.png'],
  cross: ['cross/frame_000.png', 'cross/frame_001.png', 'cross/frame_002.png', 'cross/frame_003.png', 'cross/frame_004.png', 'cross/frame_005.png'],
  jab: ['jab/frame_000.png', 'jab/frame_001.png', 'jab/frame_002.png'],
  kick: ['kick/frame_000.png', 'kick/frame_001.png', 'kick/frame_002.png', 'kick/frame_003.png', 'kick/frame_004.png', 'kick/frame_005.png', 'kick/frame_006.png'],
  fireball: ['fireball/frame_000.png', 'fireball/frame_001.png', 'fireball/frame_002.png', 'fireball/frame_003.png', 'fireball/frame_004.png', 'fireball/frame_005.png'],
  takehit: ['takehit/frame_000.png', 'takehit/frame_001.png', 'takehit/frame_002.png', 'takehit/frame_003.png', 'takehit/frame_004.png', 'takehit/frame_005.png'],
  throwobject: ['throwobject/frame_000.png', 'throwobject/frame_001.png', 'throwobject/frame_002.png', 'throwobject/frame_003.png', 'throwobject/frame_004.png', 'throwobject/frame_005.png', 'throwobject/frame_006.png'],
  uppercut: ['uppercut/frame_000.png', 'uppercut/frame_001.png', 'uppercut/frame_002.png', 'uppercut/frame_003.png', 'uppercut/frame_004.png', 'uppercut/frame_005.png', 'uppercut/frame_006.png'],
  victory: ['victory/frame_000.png', 'victory/frame_001.png', 'victory/frame_002.png', 'victory/frame_003.png', 'victory/frame_004.png', 'victory/frame_005.png', 'victory/frame_006.png', 'victory/frame_007.png', 'victory/frame_008.png', 'victory/frame_009.png', 'victory/frame_010.png', 'victory/frame_011.png', 'victory/frame_012.png'],
  getup: ['getup/frame_000.png', 'getup/frame_001.png', 'getup/frame_002.png', 'getup/frame_003.png', 'getup/frame_004.png']
};

function resolveSpriteAssetPath(relativePath) {
  return spriteAssetBase + '/' + relativePath;
}

function loadSpriteFrame(relativePath) {
  var fullPath = resolveSpriteAssetPath(relativePath);
  if (spriteImageCache[fullPath]) return spriteImageCache[fullPath];
  var img = new Image();
  img.src = fullPath;
  spriteImageCache[fullPath] = img;
  return img;
}

// AUDIO SYNTHESIZER (Touch activated for Android Chrome Mobile)
var audioCtx = null;
var userInteracted = false;

function initTouchAudioListener() {
  var activateAudio = function() {
    userInteracted = true;
    if (audioCtx && audioCtx.state === 'suspended') {
      audioCtx.resume();
    }
  };
  window.addEventListener('touchstart', activateAudio, true);
  window.addEventListener('pointerdown', activateAudio, true);
}

function ensureAudio() {
  if (!userInteracted) return;
  if (!audioCtx) {
    try {
      audioCtx = new (window.AudioContext || window.webkitAudioContext)();
    } catch(e) {}
  }
}

function playSound(freq, duration, type, volume) {
  if (!userInteracted) return;
  ensureAudio();
  if (!audioCtx) return;
  try {
    var osc = audioCtx.createOscillator();
    var gain = audioCtx.createGain();
    osc.type = type || 'sine';
    osc.frequency.setValueAtTime(freq, audioCtx.currentTime);
    gain.gain.setValueAtTime(volume || 0.15, audioCtx.currentTime);
    gain.gain.exponentialRampToValueAtTime(0.001, audioCtx.currentTime + duration);
    osc.connect(gain);
    gain.connect(audioCtx.destination);
    osc.start();
    osc.stop(audioCtx.currentTime + duration);
  } catch(e) {}
}

// SPATIAL HASH FOR PERFORMANCE
var cellSize = 80;
var spatialHash = {};

function clearSpatialHash() {
  spatialHash = {};
}

function insertToHash(body) {
  var minX = Math.floor((body.x - body.r) / cellSize);
  var maxX = Math.floor((body.x + body.r) / cellSize);
  var minY = Math.floor((body.y - body.r) / cellSize);
  var maxY = Math.floor((body.y + body.r) / cellSize);

  for (var x = minX; x <= maxX; x++) {
    for (var y = minY; y <= maxY; y++) {
      var key = x + ',' + y;
      if (!spatialHash[key]) spatialHash[key] = [];
      spatialHash[key].push(body);
    }
  }
}

// PARTICLE SYSTEM & FX
function createSparks(x, y, color, count, impulse) {
  var num = count || 8;
  var speedMult = impulse ? Math.min(impulse * 0.4, 6) : 2;
  for (var i = 0; i < num; i++) {
    var angle = Math.random() * Math.PI * 2;
    var speed = (Math.random() * 2 + 1) * speedMult;
    particles.push({
      x: x, y: y,
      vx: Math.cos(angle) * speed,
      vy: Math.sin(angle) * speed,
      life: 25 + Math.random() * 15,
      maxLife: 40,
      color: color || '#FFE37A',
      size: 2 + Math.random() * 3
    });
  }
}

function createShockwave(x, y, color) {
  shockwaves.push({
    x: x, y: y,
    r: 10,
    maxR: 90,
    alpha: 0.8,
    color: color || '#D0BCFF'
  });
  if (window.AndroidBridge && window.AndroidBridge.onShockwave) {
    window.AndroidBridge.onShockwave(Math.round(x), Math.round(y));
  }
}

function createArtisticFlairTrail(x, y) {
  var colors = ['#EADDFF', '#FFE37A', '#7AD9FF', '#D0BCFF', '#FFFFFF'];
  for (var i = 0; i < 5; i++) {
    var angle = Math.random() * Math.PI * 2;
    var speed = 0.5 + Math.random() * 2.5;
    particles.push({
      x: x + (Math.random() - 0.5) * 8,
      y: y + (Math.random() - 0.5) * 8,
      vx: Math.cos(angle) * speed,
      vy: Math.sin(angle) * speed - 0.5,
      life: 20 + Math.random() * 15,
      maxLife: 35,
      color: colors[Math.floor(Math.random() * colors.length)],
      size: 2.5 + Math.random() * 3.5,
      isFlair: true
    });
  }
}

// PHYSICS BODY CLASS
function PhysicsBody(options) {
  this.name = options.name || 'Body';
  this.type = options.type || 'box';
  this.x = options.x || 0;
  this.y = options.y || 0;
  this.w = options.w || 40;
  this.h = options.h || 40;
  this.r = options.r || Math.max(this.w, this.h) / 2;
  this.vx = options.vx || 0;
  this.vy = options.vy || 0;
  this.rot = options.rot || 0;
  this.vRot = options.vRot || 0;
  this.mass = (options.mass !== undefined) ? options.mass : 10;
  this.isStatic = options.isStatic || (this.mass === 0);
  this.restitution = options.restitution || 0.7;
  this.friction = options.friction || 0.15;
  this.color = options.color || '#6750A4';
  this.borderColor = options.borderColor || '#EADDFF';
  this.isHero = options.isHero || false;
  this.collisionCounter = 0;
  this.isDragging = false;
  this.onGround = false;
  this.shieldHP = options.shieldHP || 0;
  this.patrolBaseX = options.patrolBaseX || 0;
  this.sprite = null;
  this.spriteFacing = 1;
}

PhysicsBody.prototype.setSpriteAnimation = function(name) {
  var frames = heroSpriteAnimations[name];
  if (!frames || !frames.length) return;
  this.sprite = this.sprite || {};
  this.sprite.animationName = name;
  this.sprite.frameIndex = 0;
  this.sprite.frameTimer = 0;
  this.sprite.frameDuration = 0.09;
  this.sprite.currentFrames = frames.map(function(relativePath) {
    return loadSpriteFrame(relativePath);
  });
};

function updateHeroSprite(body, dt) {
  if (!body || body.type !== 'hero' || !body.sprite) return;

  var desiredAnim = 'idle';
  if (touchState && touchState.burstPressed) {
    desiredAnim = 'fireball';
  } else if (!body.onGround) {
    desiredAnim = 'falling';
  } else if (touchState && (Math.abs(touchState.moveX) > 0.08 || Math.abs(body.vx) > 0.8)) {
    desiredAnim = 'running';
  }

  if (body.sprite.animationName !== desiredAnim) {
    body.setSpriteAnimation(desiredAnim);
  }

  body.sprite.frameTimer += dt;
  if (body.sprite.frameTimer >= body.sprite.frameDuration) {
    body.sprite.frameTimer = 0;
    body.sprite.frameIndex = (body.sprite.frameIndex + 1) % Math.max(1, body.sprite.currentFrames.length);
  }

  if (body.vx < -0.2) {
    body.spriteFacing = -1;
  } else if (body.vx > 0.2) {
    body.spriteFacing = 1;
  }
}

// DRAW SHAPES WITH MATERIAL 3 & ENGINE MODE STYLING
PhysicsBody.prototype.draw = function(ctx) {
  ctx.save();
  ctx.translate(this.x, this.y);
  ctx.rotate(this.rot);

  // Mode Specific Shading
  if (currentMode === 'babylon') {
    ctx.shadowColor = 'rgba(255, 227, 122, 0.45)';
    ctx.shadowBlur = 18;
  } else if (currentMode === 'fabric') {
    ctx.shadowColor = 'rgba(0,0,0,0.6)';
    ctx.shadowBlur = 12;
    ctx.shadowOffsetX = 6;
    ctx.shadowOffsetY = 6;
  }

  // Draw Hero / Crate / Sphere / Diamond / Collectable / Boss
  if (this.type === 'hero') {
    if (this.sprite && this.sprite.currentFrames && this.sprite.currentFrames.length) {
      var frameImg = this.sprite.currentFrames[this.sprite.frameIndex];
      if (frameImg && frameImg.complete) {
        var spriteW = Math.max(this.w * 1.7, 60);
        var spriteH = Math.max(this.h * 1.55, 60);
        if (this.spriteFacing < 0) {
          ctx.scale(-1, 1);
        }
        ctx.drawImage(frameImg, -spriteW / 2, -spriteH / 2, spriteW, spriteH);
        ctx.restore();
        return;
      }
    }

    ctx.fillStyle = this.color;
    ctx.beginPath();
    ctx.roundRect(-this.w/2, -this.h/2, this.w, this.h, 10);
    ctx.fill();
    ctx.lineWidth = 3;
    ctx.strokeStyle = this.borderColor;
    ctx.stroke();

    // Eyes
    ctx.fillStyle = '#1C1B1F';
    ctx.beginPath();
    ctx.arc(6, -6, 3, 0, Math.PI * 2);
    ctx.arc(6, 4, 3, 0, Math.PI * 2);
    ctx.fill();
  } else if (this.type === 'sphere' || this.type === 'enemy') {
    ctx.fillStyle = this.color;
    ctx.beginPath();
    ctx.arc(0, 0, this.r, 0, Math.PI * 2);
    ctx.fill();
    ctx.lineWidth = 2.5;
    ctx.strokeStyle = this.borderColor;
    ctx.stroke();

    if (this.type === 'enemy') {
      ctx.fillStyle = '#FFE37A';
      ctx.beginPath();
      ctx.arc(0, 0, 6, 0, Math.PI * 2);
      ctx.fill();
    }
  } else if (this.type === 'diamond' || this.type === 'collectable' || this.type === 'boss') {
    ctx.fillStyle = this.color;
    ctx.beginPath();
    ctx.moveTo(0, -this.r);
    ctx.lineTo(this.r, 0);
    ctx.lineTo(0, this.r);
    ctx.lineTo(-this.r, 0);
    ctx.closePath();
    ctx.fill();
    ctx.lineWidth = 2.5;
    ctx.strokeStyle = this.borderColor;
    ctx.stroke();

    if (this.type === 'boss' && this.shieldHP > 0) {
      ctx.strokeStyle = '#FFE37A';
      ctx.lineWidth = 2;
      ctx.beginPath();
      ctx.arc(0, 0, this.r + 6, 0, Math.PI * 2);
      ctx.stroke();
    }
  } else {
    // Default box / crate / breakable
    ctx.fillStyle = this.color;
    ctx.beginPath();
    ctx.roundRect(-this.w/2, -this.h/2, this.w, this.h, 6);
    ctx.fill();
    ctx.lineWidth = 2;
    ctx.strokeStyle = this.borderColor;
    ctx.stroke();
  }

  ctx.restore();
};

// IMPULSE COLLISION RESOLVER
function resolveCollision(b1, b2, onCollisionCallback) {
  var dx = b2.x - b1.x;
  var dy = b2.y - b1.y;
  var dist = Math.sqrt(dx * dx + dy * dy);
  var minDist = b1.r + b2.r;

  if (dist < minDist && dist > 0.001) {
    var nx = dx / dist;
    var ny = dy / dist;
    var overlap = minDist - dist;

    if (!b1.isStatic && !b2.isStatic) {
      b1.x -= nx * overlap * 0.5;
      b1.y -= ny * overlap * 0.5;
      b2.x += nx * overlap * 0.5;
      b2.y += ny * overlap * 0.5;
    } else if (!b1.isStatic) {
      b1.x -= nx * overlap;
      b1.y -= ny * overlap;
    } else if (!b2.isStatic) {
      b2.x += nx * overlap;
      b2.y += ny * overlap;
    }

    var kx = b1.vx - b2.vx;
    var ky = b1.vy - b2.vy;
    var p = 2 * (nx * kx + ny * ky) / ((b1.isStatic ? 0 : 1/b1.mass) + (b2.isStatic ? 0 : 1/b2.mass));

    var rest = Math.min(b1.restitution, b2.restitution);
    if (!b1.isStatic) {
      b1.vx -= p * (1/b1.mass) * nx * (1 + rest);
      b1.vy -= p * (1/b1.mass) * ny * (1 + rest);
    }
    if (!b2.isStatic) {
      b2.vx += p * (1/b2.mass) * nx * (1 + rest);
      b2.vy += p * (1/b2.mass) * ny * (1 + rest);
    }

    var impulseScalar = Math.abs(p);
    if (impulseScalar > 0.8) {
      createSparks((b1.x + b2.x)/2, (b1.y + b2.y)/2, '#FFE37A', 10, impulseScalar);
      if (impulseScalar > 4.0) {
        createShockwave((b1.x + b2.x)/2, (b1.y + b2.y)/2, '#D0BCFF');
        playSound(180, 0.18, 'sawtooth', 0.2);
      } else {
        playSound(440, 0.08, 'sine', 0.1);
      }
    }

    if (onCollisionCallback) {
      onCollisionCallback(b1, b2, impulseScalar, nx, ny);
    }
  }
}

function initEngine(canvasElement) {
  canvas = canvasElement;
  ctx = canvas.getContext('2d');
  W = window.innerWidth;
  H = window.innerHeight;
  canvas.width = W;
  canvas.height = H;
  initTouchAudioListener();
}
