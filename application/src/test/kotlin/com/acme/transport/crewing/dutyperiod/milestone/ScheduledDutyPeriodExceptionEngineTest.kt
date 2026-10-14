package com.acme.transport.crewing.dutyperiod.milestone

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledDutyPeriodExceptionEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledDutyPeriodExceptionEngine(newRegistry())
        val item = newScheduledDutyPeriodException("code-333", "name-470")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
