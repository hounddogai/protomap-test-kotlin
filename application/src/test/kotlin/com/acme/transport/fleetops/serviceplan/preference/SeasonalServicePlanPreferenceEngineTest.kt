package com.acme.transport.fleetops.serviceplan.preference

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalServicePlanPreferenceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalServicePlanPreferenceEngine(newRegistry())
        val item = newSeasonalServicePlanPreference("code-77", "label-221")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
