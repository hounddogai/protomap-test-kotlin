package com.acme.transport.network.transittime.timenotice

import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedTransitTimeTemplateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedTransitTimeTemplateEngine()
        val item = newBatchedTransitTimeTemplate("code-420", "name-192")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
