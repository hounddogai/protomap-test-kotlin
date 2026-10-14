package com.acme.transport.freightpay.dispute.disputegroup

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RollingDisputeGroupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingDisputeGroupEngine(newRegistry())
        val item = newRollingDisputeGroup("code-929", "title-430")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
