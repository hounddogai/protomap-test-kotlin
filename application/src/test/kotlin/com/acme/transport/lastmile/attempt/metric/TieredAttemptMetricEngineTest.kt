package com.acme.transport.lastmile.attempt.metric

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TieredAttemptMetricEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredAttemptMetricEngine(newRegistry())
        val item = newTieredAttemptMetric("code-390", "label-947")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
