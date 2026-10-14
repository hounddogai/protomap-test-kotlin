package com.acme.transport.freightpay.ratecard.group

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RateCardAssignmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRateCardAssignmentEngine(newRegistry())
        val item = newRateCardAssignment("code-583", "title-750")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
