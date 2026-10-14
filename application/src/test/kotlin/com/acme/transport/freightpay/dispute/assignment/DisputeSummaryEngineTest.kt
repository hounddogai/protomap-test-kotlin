package com.acme.transport.freightpay.dispute.assignment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DisputeSummaryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDisputeSummaryEngine(newRegistry())
        val item = newDisputeSummary("code-505", "label-992")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
