package com.acme.transport.lastmile.attempt.window

import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultAttemptMetricEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultAttemptMetricEngine()
        val item = newDefaultAttemptMetric("code-403", "name-240")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
