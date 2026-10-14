package com.acme.transport.linehaul.laneplan.manifest

import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledLanePlanRecordEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledLanePlanRecordEngine()
        val item = newScheduledLanePlanRecord("code-882", "label-956")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
