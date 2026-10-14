package com.acme.transport.lastmile.proofofdelivery.baseline

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LenientProofOfDeliveryScheduleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientProofOfDeliveryScheduleEngine(newRegistry())
        val item = newLenientProofOfDeliverySchedule("code-757", "label-389")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
