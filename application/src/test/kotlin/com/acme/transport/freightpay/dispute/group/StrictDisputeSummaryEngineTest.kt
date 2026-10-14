package com.acme.transport.freightpay.dispute.group

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class StrictDisputeSummaryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictDisputeSummaryEngine(newRegistry())
        val item = newStrictDisputeSummary("code-646", "name-976")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
