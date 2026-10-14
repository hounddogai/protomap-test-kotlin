package com.acme.transport.freightpay.dispute.manifest

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RollingDisputeAdjustmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingDisputeAdjustmentEngine(newRegistry())
        val item = newRollingDisputeAdjustment("code-313", "name-891")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
