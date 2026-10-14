package com.acme.transport.freightpay.ratecard.cardledger

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RollingRateCardScheduleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingRateCardScheduleEngine(newRegistry())
        val item = newRollingRateCardSchedule("code-332", "label-545")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
