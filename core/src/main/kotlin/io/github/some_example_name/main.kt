package io.github.some_example_name

import com.badlogic.gdx.ApplicationListener
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.Texture.TextureFilter.Linear
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.Disposable
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.app.clearScreen
import ktx.assets.disposeSafely
import ktx.assets.toInternalFile
import ktx.async.KtxAsync
import ktx.graphics.use
import javax.swing.text.Position
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.input.GestureDetector
import com.badlogic.gdx.math.Vector
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Null
import com.sun.org.apache.xpath.internal.operations.Bool
import jdk.internal.vm.vector.VectorSupport
import org.w3c.dom.Text
import kotlin.collections.flatten
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

class main : KtxGame<KtxScreen>()
{
    override fun create()
    {
        KtxAsync.initiate()

        addScreen(FirstScreen())
        setScreen<FirstScreen>()
    }
}

// Coordinate System
// Y-up
// (0,0) (bottom-left) to (Gdx.graphics.getWidth() - 1, Gdx.graphics.getHeight() - 1) (top-right)

//class JoyStick
//{
//    var screenPosition: Vec2 = Vec2(0f, 0f)
//    var radius: Float = 0f
//    var deadZone: Float = 0f
//    var scaleVector: Boolean = false
//
//    constructor(screenPosition: Vec2, radius: Float, deadZone: Float, scaleVector: Boolean = false)
//    {
//        this.screenPosition = screenPosition
//        this.radius = radius
//        this.deadZone = deadZone
//        this.scaleVector = scaleVector
//    }
//
//    fun toVector(): Vec2
//    {
//        if(!Gdx.input.isTouched)
//        {
//            return Vec2(0f, 0f)
//        }
//
//        var size: Float = (screenPosition.x - Gdx.input.x.toFloat()) * (screenPosition.x - Gdx.input.x.toFloat()) + (screenPosition.y - Gdx.input.y.toFloat()) * (screenPosition.y - Gdx.input.y.toFloat())
//        size = sqrt(size)
//
//        return if(size >= deadZone)
//        {
//            return if(scaleVector)
//            {
//                val scale : Float = min(1f, size / radius)
//                Vec2((Gdx.input.x.toFloat() - screenPosition.x) / size, (Gdx.input.y.toFloat() - screenPosition.y) / size) * scale
//            }
//            else
//            {
//                Vec2((Gdx.input.x.toFloat() - screenPosition.x) / size, (Gdx.input.y.toFloat() - screenPosition.y) / size)
//            }
//        }
//        else Vec2(0f, 0f)
//    }
//}

class JoyStick : GestureDetector.GestureListener
{
    public var screenPosition : Vector2 = Vector2(100f, 100f)
    public var forwardVector : Vector2 = Vector2()

    override fun touchDown(x: Float, y: Float, pointer: Int, button: Int): Boolean
    {
        return false
    }

    override fun tap(x: Float, y: Float, count: Int, button: Int): Boolean
    {
        return false
    }

    override fun longPress(x: Float, y: Float): Boolean
    {
        return false
    }

    override fun fling(velocityX: Float, velocityY: Float, button: Int): Boolean
    {
        return false
    }

    override fun pan(x: Float, y: Float, deltaX: Float, deltaY: Float): Boolean
    {
        this.forwardVector = Vector2(x - this.screenPosition.x, y - this.screenPosition.y).nor()
        return true
    }

    override fun panStop(x: Float, y: Float, pointer: Int, button: Int): Boolean
    {
        this.forwardVector = Vector2(0f, 0f)
        return true
    }

    override fun zoom(initialDistance: Float, distance: Float): Boolean
    {
        return false
    }

    override fun pinch(initialPointer1: Vector2?, initialPointer2: Vector2?, pointer1: Vector2?, pointer2: Vector2?): Boolean
    {
        return false
    }

    override fun pinchStop()
    {
    }

}

class Transform2D(var position : Vector2 = Vector2(0f, 0f), var forward : Vector2 = Vector2(1f, 0f), var scale : Vector2 = Vector2(1f, 1f))
{
    public fun move(step : Vector2)
    {
        position = Vector2(position.x + step.x, position.y + step.y)
    }
    public fun angle() = atan2(forward.y,forward.x)
    public fun rotate(angle : Float)
    {
        forward = Vector2(cos(angle() + angle), sin(angle() + angle))
    }
}

class Animation2D : Disposable
{
    constructor(columns : Int, rows : Int, sheet : String, dt : Float)
    {
        this.columns = max(1, columns)
        this.rows = max(1, rows)

        this.sheet = Texture(sheet.toInternalFile()).apply { setFilter(Linear, Linear) }

        this.dt = min(1f, dt)

        val split = TextureRegion.split(this.sheet, this.sheet.width / this.columns, this.sheet.height / this.rows)
        var frames = com.badlogic.gdx.utils.Array<TextureRegion>()

        println(this.rows)
        println(this.columns)
        for(i in 0..(this.rows - 1))
        {
            for(j in 0..(this.columns - 1))
            {
                frames.add(split[i][j])
            }
        }

        this.animation = Animation<TextureRegion>(dt, frames, Animation.PlayMode.LOOP)
    }

    public fun update()
    {
        elapsed += Gdx.graphics.deltaTime
    }

    public fun getCurrentFrame() : TextureRegion
    {
        return animation.getKeyFrame(elapsed)
    }

    override public fun dispose()
    {
        sheet.dispose()
    }

    private var columns : Int = 1
    private var rows : Int = 1

    public var animation : Animation<TextureRegion>
    public var sheet : Texture

    private var elapsed : Float = 0f
    private var dt : Float = 1f
}

class AnimationPlayer : Disposable
{
    constructor(defaultAnimation : Animation2D)
    {
        this.defaultAnimation = defaultAnimation
    }

    constructor()
    {
    }
    public var defaultAnimation : Animation2D = Animation2D(1, 1, "nulltexture.png", 1f)
    public var animationSet : MutableMap<String, Animation2D> = mutableMapOf<String, Animation2D>()
    public var currentAnimation : Animation2D? = null

    public fun addAnimation(name : String, animation : Animation2D)
    {
        animationSet[name] = animation
    }

    public fun setCurrent(name: String)
    {
        if(animationSet.containsKey(name))
        {
            currentAnimation = animationSet.get(name)!!
        }
        else currentAnimation = defaultAnimation
    }

    public fun getCurrentFrame() : TextureRegion
    {
        return currentAnimation!!.getCurrentFrame()
    }

    public fun update()
    {
        currentAnimation!!.update()
    }

    override public fun dispose()
    {
        for(animation in animationSet)
        {
            animation.value.dispose()
        }
    }
}

operator fun AnimationPlayer.get(name: String) : Animation2D
{
    if(animationSet.containsKey(name))
    {
        return animationSet.get(name)!!
    }
    else return defaultAnimation
}

operator fun Vector2.times(other : Float) : Vector2
{
    return Vector2(x * other, y * other)
}

class CharacterBase
{
    public var sprite : AnimationPlayer = AnimationPlayer()

    public var transform : Transform2D = Transform2D()

    public var speed : Float = 500f

    public var joystick : JoyStick = JoyStick()

    constructor(sprite : AnimationPlayer, transform : Transform2D)
    {
        this.sprite = sprite
        this.transform = transform
    }

    public fun update(delta: Float)
    {
        println("("+joystick.forwardVector.x+", "+joystick.forwardVector.y+")")
        transform.move(joystick.forwardVector * speed * delta)
    }

    public fun input(delta: Float)
    {
        if(Gdx.input.isKeyPressed(Input.Keys.W))
        {
            transform.move(Vector2(0f, 1f) * speed * delta)
        }
        if(Gdx.input.isKeyPressed(Input.Keys.A))
        {
            transform.move(Vector2(-1f, 0f) * speed * delta)
        }
        if(Gdx.input.isKeyPressed(Input.Keys.S))
        {
            transform.move(Vector2(0f, -1f) * speed * delta)
        }
        if(Gdx.input.isKeyPressed(Input.Keys.D))
        {
            transform.move(Vector2(1f, 0f) * speed * delta)
        }
    }
}

class FirstScreen : KtxScreen
{
    private val image = Texture("logo.png".toInternalFile(), true).apply { setFilter(Linear, Linear) }

    private var goblinSprite : AnimationPlayer = AnimationPlayer(Animation2D(12, 1, "goblinmagespritesheet.png", 0.08f))

    private var input : JoyStick = JoyStick()

    private var goblin : CharacterBase = CharacterBase(goblinSprite, Transform2D())

    init
    {
        goblinSprite.addAnimation("idle", Animation2D(12, 1, "goblinmagespritesheet.png", 0.08f))
        goblinSprite.addAnimation("walk", Animation2D(8, 1, "goblinmagewalkspritesheet.png", 0.08f))

        goblinSprite.setCurrent("walk")

        goblin = CharacterBase(goblinSprite, Transform2D())

        Gdx.input.inputProcessor = GestureDetector(goblin.joystick)
    }

    private val batch = SpriteBatch()

    override fun render(delta: Float)
    {
        input(delta)
        clearScreen(red = 0.7f, green = 0.7f, blue = 0.7f)

        goblin.update(delta)
        goblin.sprite.update()
        batch.use {
            it.draw(goblin.sprite.getCurrentFrame(), goblin.transform.position.x, goblin.transform.position.y, 1000f, 1000f)
            it.draw(image, 100f, 160f)
        }
    }

    private fun input(delta: Float)
    {
        goblin.input(delta)
    }

    override fun dispose()
    {
        goblin.sprite.dispose()
        image.disposeSafely()
        batch.disposeSafely()
    }
}
