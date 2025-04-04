package dev.olive.utils.render.animation.impl;

/**
 * @author ChengFeng
 * @since 2024/7/29
 **/
public abstract class ComposedAnimation<T> {
    public abstract T getOutput();
    public abstract void changeDirection();
}
