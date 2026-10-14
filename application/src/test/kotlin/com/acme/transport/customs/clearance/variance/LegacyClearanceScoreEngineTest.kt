package com.acme.transport.customs.clearance.variance

import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyClearanceScoreEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyClearanceScoreEngine()
        val item = newLegacyClearanceScore("code-304", "label-205")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
