package com.acme.transport.linehaul.laneplan.checkpoint

import kotlin.test.Test
import kotlin.test.assertTrue

class LanePlanSlotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLanePlanSlotEngine()
        val item = newLanePlanSlot("code-613", "name-375")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
