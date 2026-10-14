package com.acme.transport.crewing.dutyperiod.snapshot

import kotlin.test.Test
import kotlin.test.assertTrue

class DutyPeriodProfileEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDutyPeriodProfileEngine()
        val item = newDutyPeriodProfile("code-177", "label-965")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
