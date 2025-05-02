package com.ridgebotics.ridgescout.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class BackgroundView extends View {
    private List<Circle> circles;
    private Paint whitePaint;
    private Paint greenPaint;
    private Random random;

    // Physics simulation constants
    private static final float GRAVITY = 9.8f;
    private static final float DAMPING = 0.5f;
    private static final float CIRCLE_SPAWN_INTERVAL = 500; // milliseconds
    private long lastSpawnTime = 0;

    // Screen dimensions
    private int screenWidth;
    private int screenHeight;

    public BackgroundView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    private void initialize() {
        // Setup paints
        whitePaint = new Paint();
        whitePaint.setColor(Color.WHITE);
        whitePaint.setStyle(Paint.Style.STROKE);
        whitePaint.setStrokeWidth(2);

        greenPaint = new Paint();
        greenPaint.setColor(Color.GREEN);
        greenPaint.setStyle(Paint.Style.FILL);

        circles = new ArrayList<>();
        random = new Random();

        // Get screen dimensions after layout
        ViewTreeObserver vto = getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                screenWidth = getWidth();
                screenHeight = getHeight();
                getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Spawn new circles periodically
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastSpawnTime > CIRCLE_SPAWN_INTERVAL) {
            spawnCircle();
            lastSpawnTime = currentTime;
        }

        // First pass: detect all collisions
        List<Collision> collisions = new ArrayList<>();
        for (int i = 0; i < circles.size(); i++) {
            Circle circle = circles.get(i);

            // Wall and floor collisions
            if (circle.x - circle.radius < 0) {
                circle.x = circle.radius;
                circle.velocityX *= -DAMPING;
            }
            if (circle.x + circle.radius > screenWidth) {
                circle.x = screenWidth - circle.radius;
                circle.velocityX *= -DAMPING;
            }
            if (circle.y + circle.radius > screenHeight) {
                circle.y = screenHeight - circle.radius;
                circle.velocityY = 0;
            }

            // Detect collisions with other circles
            for (int j = i + 1; j < circles.size(); j++) {
                Circle otherCircle = circles.get(j);
                float dx = otherCircle.x - circle.x;
                float dy = otherCircle.y - circle.y;
                float distance = (float) Math.sqrt(dx * dx + dy * dy);

                if (distance < circle.radius + otherCircle.radius) {
                    collisions.add(new Collision(circle, otherCircle, dx, dy, distance));
                }
            }
            
            // Apply gravity
            circle.velocityY += GRAVITY * 0.1f;
        }

        // Second pass: resolve collisions
        for (Collision collision : collisions) {
            Circle c1 = collision.circle1;
            Circle c2 = collision.circle2;
            float dx = collision.dx;
            float dy = collision.dy;
            float distance = collision.distance;

            // Calculate overlap
            float overlap = (c1.radius + c2.radius - distance) / 2;

            // Separate circles
            float separationX = overlap * dx / distance;
            float separationY = overlap * dy / distance;

            c1.x -= separationX;
            c1.y -= separationY;
            c2.x += separationX;
            c2.y += separationY;

            // Dampen velocities
            c1.velocityX *= DAMPING;
            c1.velocityY *= DAMPING;
            c2.velocityX *= DAMPING;
            c2.velocityY *= DAMPING;
        }

        // Draw and update circles
        Iterator<Circle> iterator = circles.iterator();
        while (iterator.hasNext()) {
            Circle circle = iterator.next();

            // Update position
            circle.x += circle.velocityX;
            circle.y += circle.velocityY;

            // Draw circle
            Paint paint = circle.isGreen ? greenPaint : whitePaint;
            canvas.drawCircle(circle.x, circle.y, circle.radius, paint);

            // Remove circles that have fallen off screen
            if (circle.y > screenHeight + circle.radius) {
                iterator.remove();
            }
        }

        // Trigger redraw
        invalidate();
    }

    // Collision tracking class
    private static class Collision {
        Circle circle1, circle2;
        float dx, dy, distance;

        Collision(Circle c1, Circle c2, float dx, float dy, float distance) {
            this.circle1 = c1;
            this.circle2 = c2;
            this.dx = dx;
            this.dy = dy;
            this.distance = distance;
        }
    }

    private void spawnCircle() {
        // More likely to spawn white circles
        boolean isGreen = random.nextFloat() < 0.2f;

        float radius = isGreen ?
                120:  // Green: 20-60
                80;   // White: 10-30

        float x = random.nextFloat() * screenWidth;
        float velocityX = (random.nextFloat() - 0.5f) * 5;

        circles.add(new Circle(x, -radius, radius, velocityX, 0, isGreen));
    }

    // Circle class to represent individual circles
    private static class Circle {
        float x, y;
        float radius;
        float velocityX, velocityY;
        boolean isGreen;

        Circle(float x, float y, float radius, float velocityX, float velocityY, boolean isGreen) {
            this.x = x;
            this.y = y;
            this.radius = radius;
            this.velocityX = velocityX;
            this.velocityY = velocityY;
            this.isGreen = isGreen;
        }
    }
}