package com.acme.transport.linehaul.laneplan.item

import kotlin.test.Test
import kotlin.test.assertTrue

class CappedLanePlanPreferenceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedLanePlanPreferenceEngine()
        val item = newCappedLanePlanPreference("code-836", "title-761")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
