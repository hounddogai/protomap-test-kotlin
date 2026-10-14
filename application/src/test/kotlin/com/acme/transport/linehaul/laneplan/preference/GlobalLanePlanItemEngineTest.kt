package com.acme.transport.linehaul.laneplan.preference

import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalLanePlanItemEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalLanePlanItemEngine()
        val item = newGlobalLanePlanItem("code-615", "title-478")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
