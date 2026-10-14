package com.acme.transport.freightpay.accessorial.accessorialplan

import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyAccessorialPlanEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyAccessorialPlanEngine()
        val item = newLegacyAccessorialPlan("code-580", "name-227")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
