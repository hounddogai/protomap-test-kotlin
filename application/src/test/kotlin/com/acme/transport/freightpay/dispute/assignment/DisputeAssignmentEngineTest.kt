package com.acme.transport.freightpay.dispute.assignment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DisputeAssignmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDisputeAssignmentEngine(newRegistry())
        val item = newDisputeAssignment("code-970", "label-988")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
