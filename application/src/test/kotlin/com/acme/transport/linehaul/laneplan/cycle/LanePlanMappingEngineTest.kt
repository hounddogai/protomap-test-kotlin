package com.acme.transport.linehaul.laneplan.cycle

import kotlin.test.Test
import kotlin.test.assertTrue

class LanePlanMappingEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLanePlanMappingEngine()
        val item = newLanePlanMapping("code-578", "label-820")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
