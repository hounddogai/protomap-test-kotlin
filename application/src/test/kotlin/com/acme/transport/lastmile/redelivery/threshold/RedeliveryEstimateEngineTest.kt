package com.acme.transport.lastmile.redelivery.threshold

import kotlin.test.Test
import kotlin.test.assertTrue

class RedeliveryEstimateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRedeliveryEstimateEngine()
        val item = newRedeliveryEstimate("code-240", "name-48")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
