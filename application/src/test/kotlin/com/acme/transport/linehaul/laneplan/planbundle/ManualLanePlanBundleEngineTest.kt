package com.acme.transport.linehaul.laneplan.planbundle

import kotlin.test.Test
import kotlin.test.assertTrue

class ManualLanePlanBundleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualLanePlanBundleEngine()
        val item = newManualLanePlanBundle("code-699", "label-931")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
