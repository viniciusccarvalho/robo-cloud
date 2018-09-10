if (typeof kotlin === 'undefined') {
  throw new Error("Error loading module 'dashboard'. Its dependency 'kotlin' was not found. Please, check whether 'kotlin' is loaded prior to 'dashboard'.");
}
var dashboard = function (_, Kotlin) {
  'use strict';
  var throwCCE = Kotlin.throwCCE;
  var Unit = Kotlin.kotlin.Unit;
  var math = Kotlin.kotlin.math;
  var Kind_CLASS = Kotlin.Kind.CLASS;
  var IntRange = Kotlin.kotlin.ranges.IntRange;
  var L0 = Kotlin.Long.ZERO;
  var emptyList = Kotlin.kotlin.collections.emptyList_287e2$;
  var Enum = Kotlin.kotlin.Enum;
  var throwISE = Kotlin.throwISE;
  var numberToInt = Kotlin.numberToInt;
  ArenaState.prototype = Object.create(Enum.prototype);
  ArenaState.prototype.constructor = ArenaState;
  var BORDER;
  var ANIMATION_WIDTH;
  var ANIMATIION_HEIGHT;
  var PANEL_WIDTH;
  var connected;
  function main$lambda(closure$borderWall, closure$parallax) {
    return function (it) {
      var canvasWindow = new CanvasWindow(void 0, void 0, void 0, void 0, closure$borderWall);
      canvasWindow.draw();
      closure$parallax.draw();
      return Unit;
    };
  }
  function main$lambda_0(it) {
    var button = $('#connectButton');
    connected = !connected;
    if (connected) {
      connected = true;
      button.text('Disconnect');
      button.removeClass('btn-primary');
      button.addClass('btn-danger');
    }
     else {
      connected = false;
      button.text('Connect');
      button.removeClass('btn-danger');
      button.addClass('btn-primary');
    }
    return Unit;
  }
  function main(args) {
    var tmp$, tmp$_0;
    var canvas = Kotlin.isType(tmp$ = document.getElementById('myCanvas'), HTMLCanvasElement) ? tmp$ : throwCCE();
    var context = Kotlin.isType(tmp$_0 = canvas.getContext('2d'), CanvasRenderingContext2D) ? tmp$_0 : throwCCE();
    var borderWall = new Sprite(context, 'https://opengameart.org/sites/default/files/styles/medium/public/ShmupTilesetPRE1.png', 50, 50, [[122, 7], [7, 72], [64, 72]]);
    var parallax = new Parallax(void 0, void 0, context);
    window.onload = main$lambda(borderWall, parallax);
    $('#connectButton').click(main$lambda_0);
  }
  function Sprite(ctx, path, width, height, positions) {
    this.ctx = ctx;
    this.path = path;
    this.width = width;
    this.height = height;
    this.positions = positions;
    var tmp$;
    this.image = Kotlin.isType(tmp$ = window.document.createElement('img'), HTMLImageElement) ? tmp$ : throwCCE();
    this.image.src = this.path;
  }
  Sprite.prototype.draw_hu04m1$ = function (position, x, y, angle) {
    var pos = this.positions[position];
    this.ctx.save();
    if (angle !== 0.0) {
      this.ctx.translate(x + this.width / 2.0, y + this.height / 2.0);
      this.ctx.rotate(angle * math.PI / 180);
      this.ctx.drawImage(this.image, pos[0], pos[1], this.width, this.height, (-this.width | 0) / 2.0, (-this.width | 0) / 2.0, this.width, this.height);
    }
     else {
      this.ctx.drawImage(this.image, pos[0], pos[1], this.width, this.height, x, y, this.width, this.height);
    }
    this.ctx.restore();
  };
  Sprite.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'Sprite',
    interfaces: []
  };
  function CanvasWindow(height, battleWidth, panelWidth, border, sprite) {
    if (height === void 0)
      height = 768;
    if (battleWidth === void 0)
      battleWidth = 1024;
    if (panelWidth === void 0)
      panelWidth = 320;
    if (border === void 0)
      border = 50;
    this.height = height;
    this.battleWidth = battleWidth;
    this.panelWidth = panelWidth;
    this.border = border;
    this.sprite = sprite;
    this.totalWidth = this.battleWidth + this.panelWidth + (3 * this.border | 0) | 0;
    this.totalHeight = this.height + (2 * this.border | 0) | 0;
  }
  CanvasWindow.prototype.draw = function () {
    var tmp$, tmp$_0, tmp$_1, tmp$_2, tmp$_3, tmp$_4;
    tmp$ = this.border;
    tmp$_0 = this.totalWidth - this.border | 0;
    tmp$_1 = this.border;
    for (var i = tmp$; i <= tmp$_0; i += tmp$_1) {
      this.sprite.draw_hu04m1$(0, i, 0, 0.0);
      this.sprite.draw_hu04m1$(0, i, this.totalHeight - this.border | 0, 180.0);
    }
    tmp$_2 = this.border;
    tmp$_3 = this.totalHeight - this.border | 0;
    tmp$_4 = this.border;
    for (var j = tmp$_2; j <= tmp$_3; j += tmp$_4) {
      this.sprite.draw_hu04m1$(0, 0, j, -90.0);
      this.sprite.draw_hu04m1$(0, this.totalWidth - this.border | 0, j, 90.0);
      this.sprite.draw_hu04m1$(1, this.battleWidth + BORDER | 0, j, 0.0);
    }
    this.drawCorners_0();
  };
  CanvasWindow.prototype.drawCorners_0 = function () {
    this.sprite.draw_hu04m1$(2, 0, 0, -90.0);
    this.sprite.draw_hu04m1$(2, this.totalWidth - this.border | 0, 0, 0.0);
    this.sprite.draw_hu04m1$(2, 0, this.totalHeight - this.border | 0, 180.0);
    this.sprite.draw_hu04m1$(2, this.totalWidth - this.border | 0, this.totalHeight - this.border | 0, 90.0);
  };
  CanvasWindow.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'CanvasWindow',
    interfaces: []
  };
  var ArrayList_init = Kotlin.kotlin.collections.ArrayList_init_287e2$;
  function Parallax(numLayers, baseStar, ctx) {
    if (numLayers === void 0)
      numLayers = 12;
    if (baseStar === void 0)
      baseStar = 6;
    this.numLayers = numLayers;
    this.baseStar = baseStar;
    this.ctx = ctx;
    this.started = false;
    this.bgLayers = ArrayList_init();
  }
  Parallax.prototype.initBackground_0 = function () {
    var tmp$;
    for (var i = this.numLayers; i >= 0; i--) {
      var stars = ArrayList_init();
      tmp$ = Kotlin.imul(this.baseStar, i);
      for (var j = 0; j <= tmp$; j++) {
        var star = new Star(new Position(random(new IntRange(BORDER, ANIMATION_WIDTH + BORDER | 0)), random(new IntRange(BORDER, ANIMATIION_HEIGHT + BORDER | 0))), 1.0 / i, new Position(0.0, 2.0 / i));
        stars.add_11rb$(star);
      }
      this.bgLayers.add_11rb$(stars);
    }
  };
  Parallax.prototype.render = function () {
    var tmp$, tmp$_0;
    this.ctx.fillStyle = '#000000';
    this.ctx.strokeStyle = '#FFFFFF';
    this.ctx.fillRect(BORDER + 1.0, BORDER + 1.0, ANIMATION_WIDTH - 1.0, ANIMATIION_HEIGHT - 1.0);
    this.ctx.save();
    this.ctx.strokeStyle = '#FFFFFF';
    this.ctx.fillStyle = '#FFFFFF';
    tmp$ = this.bgLayers.size;
    for (var i = 0; i < tmp$; i++) {
      var stars = this.bgLayers.get_za3lpa$(i);
      tmp$_0 = stars.size;
      for (var j = 0; j < tmp$_0; j++) {
        var star = stars.get_za3lpa$(j);
        this.ctx.beginPath();
        this.ctx.arc(star.position.x, star.position.y, star.radius, 0.0, Math.PI * 2);
        this.ctx.fill();
        this.ctx.stroke();
      }
    }
    this.ctx.restore();
  };
  Parallax.prototype.update_s8cxhz$ = function (delta) {
    var tmp$, tmp$_0;
    if (!this.started) {
      this.started = true;
      this.initBackground_0();
    }
     else {
      tmp$ = this.bgLayers.size;
      for (var i = 0; i < tmp$; i++) {
        var stars = this.bgLayers.get_za3lpa$(i);
        tmp$_0 = stars.size;
        for (var j = 0; j < tmp$_0; j++) {
          var star = stars.get_za3lpa$(j);
          if (this.inBounds_0(star.position)) {
            star.position.x = star.position.x + star.speed.x;
            star.position.y = star.position.y + star.speed.y;
          }
           else {
            star.position.y = 1.0 + BORDER;
            star.position.x = random(new IntRange(BORDER, ANIMATION_WIDTH + BORDER | 0));
          }
        }
      }
    }
  };
  function Parallax$draw$lambda(this$Parallax) {
    return function () {
      this$Parallax.draw();
      return Unit;
    };
  }
  Parallax.prototype.draw = function () {
    this.update_s8cxhz$(L0);
    this.render();
    window.setTimeout(Parallax$draw$lambda(this), 30);
  };
  Parallax.prototype.inBounds_0 = function (position) {
    return position.x > BORDER && position.x < (BORDER + ANIMATION_WIDTH | 0) && (position.y > BORDER && position.y < (BORDER + ANIMATIION_HEIGHT | 0));
  };
  Parallax.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'Parallax',
    interfaces: []
  };
  function Position(x, y) {
    if (x === void 0)
      x = 0.0;
    if (y === void 0)
      y = 0.0;
    this.x = x;
    this.y = y;
  }
  Position.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'Position',
    interfaces: []
  };
  function Star(position, radius, speed) {
    if (radius === void 0)
      radius = 0.0;
    this.position = position;
    this.radius = radius;
    this.speed = speed;
  }
  Star.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'Star',
    interfaces: []
  };
  function Coordinates(x, y) {
    this.x = x;
    this.y = y;
  }
  Coordinates.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'Coordinates',
    interfaces: []
  };
  Coordinates.prototype.component1 = function () {
    return this.x;
  };
  Coordinates.prototype.component2 = function () {
    return this.y;
  };
  Coordinates.prototype.copy_vux9f0$ = function (x, y) {
    return new Coordinates(x === void 0 ? this.x : x, y === void 0 ? this.y : y);
  };
  Coordinates.prototype.toString = function () {
    return 'Coordinates(x=' + Kotlin.toString(this.x) + (', y=' + Kotlin.toString(this.y)) + ')';
  };
  Coordinates.prototype.hashCode = function () {
    var result = 0;
    result = result * 31 + Kotlin.hashCode(this.x) | 0;
    result = result * 31 + Kotlin.hashCode(this.y) | 0;
    return result;
  };
  Coordinates.prototype.equals = function (other) {
    return this === other || (other !== null && (typeof other === 'object' && (Object.getPrototypeOf(this) === Object.getPrototypeOf(other) && (Kotlin.equals(this.x, other.x) && Kotlin.equals(this.y, other.y)))));
  };
  function Box(bearing, coordinates) {
    this.bearing = bearing;
    this.coordinates = coordinates;
  }
  Box.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'Box',
    interfaces: []
  };
  Box.prototype.component1 = function () {
    return this.bearing;
  };
  Box.prototype.component2 = function () {
    return this.coordinates;
  };
  Box.prototype.copy_w81whq$ = function (bearing, coordinates) {
    return new Box(bearing === void 0 ? this.bearing : bearing, coordinates === void 0 ? this.coordinates : coordinates);
  };
  Box.prototype.toString = function () {
    return 'Box(bearing=' + Kotlin.toString(this.bearing) + (', coordinates=' + Kotlin.toString(this.coordinates)) + ')';
  };
  Box.prototype.hashCode = function () {
    var result = 0;
    result = result * 31 + Kotlin.hashCode(this.bearing) | 0;
    result = result * 31 + Kotlin.hashCode(this.coordinates) | 0;
    return result;
  };
  Box.prototype.equals = function (other) {
    return this === other || (other !== null && (typeof other === 'object' && (Object.getPrototypeOf(this) === Object.getPrototypeOf(other) && (Kotlin.equals(this.bearing, other.bearing) && Kotlin.equals(this.coordinates, other.coordinates)))));
  };
  function Robot(id, name, box, radar, health, score) {
    if (radar === void 0)
      radar = emptyList();
    this.id = id;
    this.name = name;
    this.box = box;
    this.radar = radar;
    this.health = health;
    this.score = score;
  }
  Robot.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'Robot',
    interfaces: []
  };
  Robot.prototype.component1 = function () {
    return this.id;
  };
  Robot.prototype.component2 = function () {
    return this.name;
  };
  Robot.prototype.component3 = function () {
    return this.box;
  };
  Robot.prototype.component4 = function () {
    return this.radar;
  };
  Robot.prototype.component5 = function () {
    return this.health;
  };
  Robot.prototype.component6 = function () {
    return this.score;
  };
  Robot.prototype.copy_rky5yf$ = function (id, name, box, radar, health, score) {
    return new Robot(id === void 0 ? this.id : id, name === void 0 ? this.name : name, box === void 0 ? this.box : box, radar === void 0 ? this.radar : radar, health === void 0 ? this.health : health, score === void 0 ? this.score : score);
  };
  Robot.prototype.toString = function () {
    return 'Robot(id=' + Kotlin.toString(this.id) + (', name=' + Kotlin.toString(this.name)) + (', box=' + Kotlin.toString(this.box)) + (', radar=' + Kotlin.toString(this.radar)) + (', health=' + Kotlin.toString(this.health)) + (', score=' + Kotlin.toString(this.score)) + ')';
  };
  Robot.prototype.hashCode = function () {
    var result = 0;
    result = result * 31 + Kotlin.hashCode(this.id) | 0;
    result = result * 31 + Kotlin.hashCode(this.name) | 0;
    result = result * 31 + Kotlin.hashCode(this.box) | 0;
    result = result * 31 + Kotlin.hashCode(this.radar) | 0;
    result = result * 31 + Kotlin.hashCode(this.health) | 0;
    result = result * 31 + Kotlin.hashCode(this.score) | 0;
    return result;
  };
  Robot.prototype.equals = function (other) {
    return this === other || (other !== null && (typeof other === 'object' && (Object.getPrototypeOf(this) === Object.getPrototypeOf(other) && (Kotlin.equals(this.id, other.id) && Kotlin.equals(this.name, other.name) && Kotlin.equals(this.box, other.box) && Kotlin.equals(this.radar, other.radar) && Kotlin.equals(this.health, other.health) && Kotlin.equals(this.score, other.score)))));
  };
  function Projectile(id, robotId, box) {
    this.id = id;
    this.robotId = robotId;
    this.box = box;
  }
  Projectile.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'Projectile',
    interfaces: []
  };
  Projectile.prototype.component1 = function () {
    return this.id;
  };
  Projectile.prototype.component2 = function () {
    return this.robotId;
  };
  Projectile.prototype.component3 = function () {
    return this.box;
  };
  Projectile.prototype.copy_q9q4wf$ = function (id, robotId, box) {
    return new Projectile(id === void 0 ? this.id : id, robotId === void 0 ? this.robotId : robotId, box === void 0 ? this.box : box);
  };
  Projectile.prototype.toString = function () {
    return 'Projectile(id=' + Kotlin.toString(this.id) + (', robotId=' + Kotlin.toString(this.robotId)) + (', box=' + Kotlin.toString(this.box)) + ')';
  };
  Projectile.prototype.hashCode = function () {
    var result = 0;
    result = result * 31 + Kotlin.hashCode(this.id) | 0;
    result = result * 31 + Kotlin.hashCode(this.robotId) | 0;
    result = result * 31 + Kotlin.hashCode(this.box) | 0;
    return result;
  };
  Projectile.prototype.equals = function (other) {
    return this === other || (other !== null && (typeof other === 'object' && (Object.getPrototypeOf(this) === Object.getPrototypeOf(other) && (Kotlin.equals(this.id, other.id) && Kotlin.equals(this.robotId, other.robotId) && Kotlin.equals(this.box, other.box)))));
  };
  function ArenaView(id, state, timestamp, robots, projectiles) {
    this.id = id;
    this.state = state;
    this.timestamp = timestamp;
    this.robots = robots;
    this.projectiles = projectiles;
  }
  ArenaView.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'ArenaView',
    interfaces: []
  };
  ArenaView.prototype.component1 = function () {
    return this.id;
  };
  ArenaView.prototype.component2 = function () {
    return this.state;
  };
  ArenaView.prototype.component3 = function () {
    return this.timestamp;
  };
  ArenaView.prototype.component4 = function () {
    return this.robots;
  };
  ArenaView.prototype.component5 = function () {
    return this.projectiles;
  };
  ArenaView.prototype.copy_vq14de$ = function (id, state, timestamp, robots, projectiles) {
    return new ArenaView(id === void 0 ? this.id : id, state === void 0 ? this.state : state, timestamp === void 0 ? this.timestamp : timestamp, robots === void 0 ? this.robots : robots, projectiles === void 0 ? this.projectiles : projectiles);
  };
  ArenaView.prototype.toString = function () {
    return 'ArenaView(id=' + Kotlin.toString(this.id) + (', state=' + Kotlin.toString(this.state)) + (', timestamp=' + Kotlin.toString(this.timestamp)) + (', robots=' + Kotlin.toString(this.robots)) + (', projectiles=' + Kotlin.toString(this.projectiles)) + ')';
  };
  ArenaView.prototype.hashCode = function () {
    var result = 0;
    result = result * 31 + Kotlin.hashCode(this.id) | 0;
    result = result * 31 + Kotlin.hashCode(this.state) | 0;
    result = result * 31 + Kotlin.hashCode(this.timestamp) | 0;
    result = result * 31 + Kotlin.hashCode(this.robots) | 0;
    result = result * 31 + Kotlin.hashCode(this.projectiles) | 0;
    return result;
  };
  ArenaView.prototype.equals = function (other) {
    return this === other || (other !== null && (typeof other === 'object' && (Object.getPrototypeOf(this) === Object.getPrototypeOf(other) && (Kotlin.equals(this.id, other.id) && Kotlin.equals(this.state, other.state) && Kotlin.equals(this.timestamp, other.timestamp) && Kotlin.equals(this.robots, other.robots) && Kotlin.equals(this.projectiles, other.projectiles)))));
  };
  function ArenaState(name, ordinal) {
    Enum.call(this);
    this.name$ = name;
    this.ordinal$ = ordinal;
  }
  function ArenaState_initFields() {
    ArenaState_initFields = function () {
    };
    ArenaState$STARTED_instance = new ArenaState('STARTED', 0);
    ArenaState$WAITING_FOR_PLAYERS_instance = new ArenaState('WAITING_FOR_PLAYERS', 1);
    ArenaState$SIMULATION_RUNNING_instance = new ArenaState('SIMULATION_RUNNING', 2);
    ArenaState$OVER_instance = new ArenaState('OVER', 3);
    ArenaState$STOPPED_instance = new ArenaState('STOPPED', 4);
  }
  var ArenaState$STARTED_instance;
  function ArenaState$STARTED_getInstance() {
    ArenaState_initFields();
    return ArenaState$STARTED_instance;
  }
  var ArenaState$WAITING_FOR_PLAYERS_instance;
  function ArenaState$WAITING_FOR_PLAYERS_getInstance() {
    ArenaState_initFields();
    return ArenaState$WAITING_FOR_PLAYERS_instance;
  }
  var ArenaState$SIMULATION_RUNNING_instance;
  function ArenaState$SIMULATION_RUNNING_getInstance() {
    ArenaState_initFields();
    return ArenaState$SIMULATION_RUNNING_instance;
  }
  var ArenaState$OVER_instance;
  function ArenaState$OVER_getInstance() {
    ArenaState_initFields();
    return ArenaState$OVER_instance;
  }
  var ArenaState$STOPPED_instance;
  function ArenaState$STOPPED_getInstance() {
    ArenaState_initFields();
    return ArenaState$STOPPED_instance;
  }
  ArenaState.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'ArenaState',
    interfaces: [Enum]
  };
  function ArenaState$values() {
    return [ArenaState$STARTED_getInstance(), ArenaState$WAITING_FOR_PLAYERS_getInstance(), ArenaState$SIMULATION_RUNNING_getInstance(), ArenaState$OVER_getInstance(), ArenaState$STOPPED_getInstance()];
  }
  ArenaState.values = ArenaState$values;
  function ArenaState$valueOf(name) {
    switch (name) {
      case 'STARTED':
        return ArenaState$STARTED_getInstance();
      case 'WAITING_FOR_PLAYERS':
        return ArenaState$WAITING_FOR_PLAYERS_getInstance();
      case 'SIMULATION_RUNNING':
        return ArenaState$SIMULATION_RUNNING_getInstance();
      case 'OVER':
        return ArenaState$OVER_getInstance();
      case 'STOPPED':
        return ArenaState$STOPPED_getInstance();
      default:throwISE('No enum constant ArenaState.' + name);
    }
  }
  ArenaState.valueOf_61zpoe$ = ArenaState$valueOf;
  function random($receiver) {
    return numberToInt(Math.random() * ($receiver.endInclusive + 1 - $receiver.start | 0) + $receiver.start);
  }
  Object.defineProperty(_, 'BORDER', {
    get: function () {
      return BORDER;
    }
  });
  Object.defineProperty(_, 'ANIMATION_WIDTH', {
    get: function () {
      return ANIMATION_WIDTH;
    }
  });
  Object.defineProperty(_, 'ANIMATIION_HEIGHT', {
    get: function () {
      return ANIMATIION_HEIGHT;
    }
  });
  Object.defineProperty(_, 'PANEL_WIDTH', {
    get: function () {
      return PANEL_WIDTH;
    }
  });
  Object.defineProperty(_, 'connected', {
    get: function () {
      return connected;
    },
    set: function (value) {
      connected = value;
    }
  });
  _.main_kand9s$ = main;
  _.Sprite = Sprite;
  _.CanvasWindow = CanvasWindow;
  _.Parallax = Parallax;
  _.Position = Position;
  _.Star = Star;
  _.Coordinates = Coordinates;
  _.Box = Box;
  _.Robot = Robot;
  _.Projectile = Projectile;
  _.ArenaView = ArenaView;
  Object.defineProperty(ArenaState, 'STARTED', {
    get: ArenaState$STARTED_getInstance
  });
  Object.defineProperty(ArenaState, 'WAITING_FOR_PLAYERS', {
    get: ArenaState$WAITING_FOR_PLAYERS_getInstance
  });
  Object.defineProperty(ArenaState, 'SIMULATION_RUNNING', {
    get: ArenaState$SIMULATION_RUNNING_getInstance
  });
  Object.defineProperty(ArenaState, 'OVER', {
    get: ArenaState$OVER_getInstance
  });
  Object.defineProperty(ArenaState, 'STOPPED', {
    get: ArenaState$STOPPED_getInstance
  });
  _.ArenaState = ArenaState;
  _.random_w88xvb$ = random;
  BORDER = 50;
  ANIMATION_WIDTH = 1024;
  ANIMATIION_HEIGHT = 768;
  PANEL_WIDTH = 320;
  connected = false;
  main([]);
  Kotlin.defineModule('dashboard', _);
  return _;
}(typeof dashboard === 'undefined' ? {} : dashboard, kotlin);

//# sourceMappingURL=dashboard.js.map
