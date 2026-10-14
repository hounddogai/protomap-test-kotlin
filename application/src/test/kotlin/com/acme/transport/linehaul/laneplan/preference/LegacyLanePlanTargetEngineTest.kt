package com.acme.transport.linehaul.laneplan.preference

import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyLanePlanTargetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyLanePlanTargetEngine()
        val item = newLegacyLanePlanTarget("code-837", "label-471")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
