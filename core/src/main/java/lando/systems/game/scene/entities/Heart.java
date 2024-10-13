package lando.systems.game.scene.entities;

public class Heart {

//    private final Entity entity;
//    private final Position position;
//    private final Vector2 velocity;
//    private final Image image;
//
//    private final TextureRegion heart;
//    private final TextureRegion heartBroken;
//
//    public Heart(Assets assets, Entity entity, int x, int y) {
//        this.entity = entity;
//        this.heart = assets.get(Icons.class, Icons.Type.HEART);
//        this.heartBroken = assets.get(Icons.class, Icons.Type.HEART_BROKEN);
//        this.position = new Position(entity, x, y);
//        this.velocity = new Vector2(0, 0);
//        this.image = new Image(entity, heart);
//        this.image.bounds.setPosition(position.value);
//    }
//
//    public void randomizeVelocity() {
//        var angle = MathUtils.random(0f, 360f);
//        var speed = MathUtils.random(10f, 100f);
//        velocity.set(
//            MathUtils.cosDeg(angle) * speed,
//            MathUtils.sinDeg(angle) * speed);
//    }
//
//    public void update(float dt) {
//        position.move(
//            velocity.x * dt,
//            velocity.y * dt);
//        image.bounds.setPosition(position.value);
//
//        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
//            randomizeVelocity();
//
//            // TODO(brian): on collision, not keypress
//            if (image.region == heart) {
//                image.region = heartBroken;
//            } else {
//                image.region = heart;
//            }
//        }
//    }
//
//    public void render(SpriteBatch batch) {
//        image.render(batch);
//    }
//
//    public void render(ShapeDrawer shapes) {
//        var radius = 3f;
//        shapes.filledCircle(position.value, radius, Color.CYAN);
//        shapes.filledCircle(position.value, radius * (2 / 3f), Color.MAGENTA);
//    }
}
