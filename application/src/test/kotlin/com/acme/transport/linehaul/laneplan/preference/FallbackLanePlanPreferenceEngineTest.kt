package com.acme.transport.linehaul.laneplan.preference

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackLanePlanPreferenceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackLanePlanPreferenceEngine(newRegistry())
        val item = newFallbackLanePlanPreference("code-13", "name-684")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
