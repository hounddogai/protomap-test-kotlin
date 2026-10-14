package com.acme.transport.crewing.dutyperiod.group

import kotlin.test.Test
import kotlin.test.assertTrue

class DutyPeriodGroupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDutyPeriodGroupEngine()
        val item = newDutyPeriodGroup("code-983", "title-636")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
