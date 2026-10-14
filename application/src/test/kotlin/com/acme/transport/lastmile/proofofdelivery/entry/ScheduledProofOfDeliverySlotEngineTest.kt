package com.acme.transport.lastmile.proofofdelivery.entry

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledProofOfDeliverySlotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledProofOfDeliverySlotEngine(newRegistry())
        val item = newScheduledProofOfDeliverySlot("code-176", "name-811")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
