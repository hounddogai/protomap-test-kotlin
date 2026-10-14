package com.acme.transport.freightpay.ratecard.regionalcardsnapshot

import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalRateCardAssignmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalRateCardAssignmentEngine()
        val item = newGlobalRateCardAssignment("code-92", "name-42")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
