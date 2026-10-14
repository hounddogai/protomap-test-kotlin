package com.acme.transport.crewing.dutyperiod.ledger

import kotlin.test.Test
import kotlin.test.assertTrue

class LenientDutyPeriodProfileEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientDutyPeriodProfileEngine()
        val item = newLenientDutyPeriodProfile("code-500", "name-862")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
