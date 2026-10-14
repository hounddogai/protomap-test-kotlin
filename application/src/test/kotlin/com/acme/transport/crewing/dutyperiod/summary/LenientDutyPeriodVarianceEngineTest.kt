package com.acme.transport.crewing.dutyperiod.summary

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LenientDutyPeriodVarianceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientDutyPeriodVarianceEngine(newRegistry())
        val item = newLenientDutyPeriodVariance("code-758", "title-382")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
