if (typeof kotlin === 'undefined') {
  throw new Error("Error loading module 'dashboard'. Its dependency 'kotlin' was not found. Please, check whether 'kotlin' is loaded prior to 'dashboard'.");
}
var dashboard = function (_, Kotlin) {
  'use strict';
  var throwCCE = Kotlin.throwCCE;
  var Unit = Kotlin.kotlin.Unit;
  var toString = Kotlin.toString;
  var math = Kotlin.kotlin.math;
  var Kind_CLASS = Kotlin.Kind.CLASS;
  var IntRange = Kotlin.kotlin.ranges.IntRange;
  var L0 = Kotlin.Long.ZERO;
  var abs = Kotlin.kotlin.math.abs_za3lpa$;
  var StringBuilder_init = Kotlin.kotlin.text.StringBuilder_init;
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
  var canvas;
  var context;
  var ship;
  var borderWall;
  var canvasWindow;
  var parallax;
  var statusWindow;
  function main$lambda(it) {
    canvasWindow.draw();
    return Unit;
  }
  function main$lambda$lambda(event) {
    var message = event;
    var view = JSON.parse(toString(message.data));
    onViewUpdate(view);
    return Unit;
  }
  function main$lambda_0(it) {
    var button = $('#connectButton');
    connected = !connected;
    if (connected) {
      var socket = new WebSocket('ws://localhost:8080/arenas');
      socket.onmessage = main$lambda$lambda;
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
    window.onload = main$lambda;
    $('#connectButton').click(main$lambda_0);
  }
  function onViewUpdate(view) {
    parallax.draw();
    var $receiver = view.robots;
    var tmp$;
    for (tmp$ = 0; tmp$ !== $receiver.length; ++tmp$) {
      var element = $receiver[tmp$];
      ship.draw_gb4hak$(0, element.box.coordinates.x - 32 + 50 | 0, element.box.coordinates.y - 32 + 50 | 0, element.box.bearing);
      statusWindow.updateBot_1b43m2$(element);
    }
    var $receiver_0 = view.projectiles;
    var tmp$_0;
    for (tmp$_0 = 0; tmp$_0 !== $receiver_0.length; ++tmp$_0) {
      var element_0 = $receiver_0[tmp$_0];
      drawProjectile(element_0);
    }
    statusWindow.render();
    canvasWindow.draw();
  }
  function drawProjectile(projectile) {
    context.save();
    context.beginPath();
    context.fillStyle = '#ffff00';
    context.translate(projectile.box.coordinates.x + 18, projectile.box.coordinates.y + 18);
    context.arc(0.0, 0.0, 3.0, 0.0, 2 * math.PI);
    context.fill();
    context.restore();
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
  Sprite.prototype.draw_gb4hak$ = function (position, x, y, angle) {
    var pos = this.positions[position];
    this.ctx.save();
    this.ctx.translate(x + this.width / 2.0, y + this.height / 2.0);
    this.ctx.rotate(angle);
    this.ctx.drawImage(this.image, pos[0], pos[1], this.width, this.height, (-this.width | 0) / 2.0, (-this.height | 0) / 2.0, this.width, this.height);
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
      this.sprite.draw_gb4hak$(0, i, 0, 0.0);
      this.sprite.draw_gb4hak$(0, i, this.totalHeight - this.border | 0, 180.0 * math.PI / 180.0);
    }
    tmp$_2 = this.border;
    tmp$_3 = this.totalHeight - this.border | 0;
    tmp$_4 = this.border;
    for (var j = tmp$_2; j <= tmp$_3; j += tmp$_4) {
      this.sprite.draw_gb4hak$(0, 0, j, -90.0 * math.PI / 180.0);
      this.sprite.draw_gb4hak$(0, this.totalWidth - this.border | 0, j, 90.0 * math.PI / 180.0);
      this.sprite.draw_gb4hak$(1, this.battleWidth + 50 | 0, j, 0.0);
    }
    this.drawCorners_0();
  };
  CanvasWindow.prototype.drawCorners_0 = function () {
    this.sprite.draw_gb4hak$(2, 0, 0, -90.0 * math.PI / 180.0);
    this.sprite.draw_gb4hak$(2, this.totalWidth - this.border | 0, 0, 0.0);
    this.sprite.draw_gb4hak$(2, 0, this.totalHeight - this.border | 0, 180.0 * math.PI / 180.0);
    this.sprite.draw_gb4hak$(2, this.totalWidth - this.border | 0, this.totalHeight - this.border | 0, 90.0 * math.PI / 180.0);
  };
  CanvasWindow.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'CanvasWindow',
    interfaces: []
  };
  var LinkedHashMap_init = Kotlin.kotlin.collections.LinkedHashMap_init_q3lmfv$;
  function StatusWindow(ctx, marginLeft, marginTop) {
    this.ctx = ctx;
    this.marginLeft = marginLeft;
    this.marginTop = marginTop;
    this.bots = LinkedHashMap_init();
  }
  StatusWindow.prototype.render = function () {
    this.ctx.clearRect(this.marginLeft, this.marginTop, 320, 768);
    var tmp$;
    tmp$ = this.bots.values.iterator();
    while (tmp$.hasNext()) {
      var element = tmp$.next();
      element.render_f69bme$(this.ctx);
    }
  };
  StatusWindow.prototype.updateBot_1b43m2$ = function (bot) {
    var tmp$;
    if (this.bots.containsKey_11rb$(bot.id)) {
      (tmp$ = this.bots.get_11rb$(bot.id)) != null ? (tmp$.robotState = bot) : null;
    }
     else {
      var $receiver = this.bots;
      var key = bot.id;
      var value = new BotStatus(bot, this.bots.size, this.marginLeft, this.marginTop);
      $receiver.put_xwzc9p$(key, value);
    }
  };
  StatusWindow.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'StatusWindow',
    interfaces: []
  };
  function BotStatus(robotState, index, marginLeft, marginTop) {
    this.robotState = robotState;
    this.index = index;
    this.marginLeft = marginLeft;
    this.marginTop = marginTop;
    this.margin = 10;
    this.height = 50;
    this.fontHeight = 9.0;
    this.maxHealthSize = 100;
    this.targetHealthSize = this.maxHealthSize;
    this.barWidth = 102.0;
    this.barHeight = 14.0;
    this.healthHeight = 12.0;
  }
  BotStatus.prototype.render_f69bme$ = function (ctx) {
    this.drawEnergyBar_0(ctx);
    this.drawText_0(ctx);
  };
  var Math_0 = Math;
  BotStatus.prototype.drawEnergyBar_0 = function (ctx) {
    var currentHealthSize = this.robotState.health / 100 * this.maxHealthSize;
    var barMarginTop = (this.marginTop + Kotlin.imul(this.height, this.index) + 18 | 0) + this.fontHeight;
    if (this.targetHealthSize - currentHealthSize >= 0) {
      var b = (this.targetHealthSize = this.targetHealthSize - 1 | 0, this.targetHealthSize);
      this.targetHealthSize = Math_0.max(0, b);
    }
    ctx.save();
    ctx.strokeStyle = '#ffffff';
    ctx.strokeRect(this.marginLeft + 80.0, barMarginTop, this.barWidth, this.barHeight);
    ctx.fillStyle = '#ff0000';
    ctx.fillRect(this.marginLeft + 81.0, barMarginTop + 1, this.maxHealthSize, this.healthHeight);
    ctx.fillStyle = 'yellow';
    ctx.fillRect(this.marginLeft + 81.0, barMarginTop + 1, this.targetHealthSize + 1, this.healthHeight);
    ctx.fill();
    ctx.stroke();
    ctx.restore();
  };
  BotStatus.prototype.drawText_0 = function (ctx) {
    var rightColumnOffset = (this.marginLeft + 320 - this.margin | 0) - 100.0;
    var leftColumnOffset = this.margin + this.marginLeft;
    var firstRowOffset = (this.marginTop + this.margin | 0) + this.fontHeight + Kotlin.imul(this.height, this.index);
    var secondRowOffset = (this.marginTop + this.margin | 0) + this.fontHeight + Kotlin.imul(this.height, this.index) + 20.0;
    ctx.save();
    ctx.fillStyle = '#ffffff';
    ctx.font = '8px "Press Start 2P"';
    ctx.fillText(this.robotState.name, leftColumnOffset, firstRowOffset);
    ctx.fillText('Health :', leftColumnOffset, secondRowOffset);
    ctx.fillText('Score : ' + formatScore(this.robotState.score), rightColumnOffset, firstRowOffset);
    ctx.fillText('Ammo  :', rightColumnOffset, secondRowOffset);
    ctx.fill();
    ctx.restore();
  };
  BotStatus.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'BotStatus',
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
        var star = new Star(new Position(random(new IntRange(50, 1074)), random(new IntRange(50, 818))), 1.0 / i, new Position(0.0, 2.0 / i));
        stars.add_11rb$(star);
      }
      this.bgLayers.add_11rb$(stars);
    }
  };
  Parallax.prototype.render = function () {
    var tmp$, tmp$_0;
    this.ctx.fillStyle = '#000000';
    this.ctx.strokeStyle = '#FFFFFF';
    this.ctx.fillRect(50 + 1.0, 50 + 1.0, 1024 - 1.0, 768 - 1.0);
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
            star.position.y = 1.0 + 50;
            star.position.x = random(new IntRange(50, 1074));
          }
        }
      }
    }
  };
  Parallax.prototype.draw = function () {
    this.update_s8cxhz$(L0);
    this.render();
  };
  Parallax.prototype.inBounds_0 = function (position) {
    return position.x > 50 && position.x < 1074 && (position.y > 50 && position.y < 818);
  };
  Parallax.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'Parallax',
    interfaces: []
  };
  function formatScore(score) {
    var scoreText = abs(score).toString();
    var builder = StringBuilder_init();
    if (score < 0) {
      builder.append_gw00v9$('-');
    }
    for (var i = scoreText.length; i <= 3; i++) {
      builder.append_gw00v9$('0');
    }
    builder.append_gw00v9$(scoreText);
    return builder.toString();
  }
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
  ArenaView.prototype.copy_4jj2p0$ = function (id, state, timestamp, robots, projectiles) {
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
  Object.defineProperty(_, 'canvas', {
    get: function () {
      return canvas;
    }
  });
  Object.defineProperty(_, 'context', {
    get: function () {
      return context;
    }
  });
  Object.defineProperty(_, 'ship', {
    get: function () {
      return ship;
    }
  });
  Object.defineProperty(_, 'borderWall', {
    get: function () {
      return borderWall;
    }
  });
  Object.defineProperty(_, 'canvasWindow', {
    get: function () {
      return canvasWindow;
    }
  });
  Object.defineProperty(_, 'parallax', {
    get: function () {
      return parallax;
    }
  });
  Object.defineProperty(_, 'statusWindow', {
    get: function () {
      return statusWindow;
    }
  });
  _.main_kand9s$ = main;
  _.onViewUpdate_xdrcno$ = onViewUpdate;
  _.drawProjectile_dzs3t$ = drawProjectile;
  _.Sprite = Sprite;
  _.CanvasWindow = CanvasWindow;
  _.StatusWindow = StatusWindow;
  _.BotStatus = BotStatus;
  _.Parallax = Parallax;
  _.formatScore_za3lpa$ = formatScore;
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
  var tmp$, tmp$_0;
  canvas = Kotlin.isType(tmp$ = document.getElementById('myCanvas'), HTMLCanvasElement) ? tmp$ : throwCCE();
  context = Kotlin.isType(tmp$_0 = canvas.getContext('2d'), CanvasRenderingContext2D) ? tmp$_0 : throwCCE();
  ship = new Sprite(context, 'images/ship.png', 64, 64, [[0, 0]]);
  borderWall = new Sprite(context, 'images/tilesetpr.png', 50, 50, [[122, 7], [7, 72], [64, 72]]);
  canvasWindow = new CanvasWindow(void 0, void 0, void 0, void 0, borderWall);
  parallax = new Parallax(void 0, void 0, context);
  statusWindow = new StatusWindow(context, 1124, 50);
  main([]);
  Kotlin.defineModule('dashboard', _);
  return _;
}(typeof dashboard === 'undefined' ? {} : dashboard, kotlin);

//# sourceMappingURL=dashboard.js.map
