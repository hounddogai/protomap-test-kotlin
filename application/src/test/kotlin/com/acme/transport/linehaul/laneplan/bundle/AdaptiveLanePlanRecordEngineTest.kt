package com.acme.transport.linehaul.laneplan.bundle

import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveLanePlanRecordEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveLanePlanRecordEngine()
        val item = newAdaptiveLanePlanRecord("code-218", "name-740")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
