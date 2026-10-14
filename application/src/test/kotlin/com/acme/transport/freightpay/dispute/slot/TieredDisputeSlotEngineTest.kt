package com.acme.transport.freightpay.dispute.slot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TieredDisputeSlotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredDisputeSlotEngine(newRegistry())
        val item = newTieredDisputeSlot("code-155", "label-308")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
