package com.acme.transport.customs.clearance.estimate

import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryClearanceVarianceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryClearanceVarianceEngine()
        val item = newPrimaryClearanceVariance("code-914", "title-14")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
