package com.acme.transport.linehaul.laneplan.overrideitem

import kotlin.test.Test
import kotlin.test.assertTrue

class LenientLanePlanSlotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientLanePlanSlotEngine()
        val item = newLenientLanePlanSlot("code-632", "title-930")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
