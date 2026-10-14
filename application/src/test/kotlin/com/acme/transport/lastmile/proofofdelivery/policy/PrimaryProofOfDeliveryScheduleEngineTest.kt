package com.acme.transport.lastmile.proofofdelivery.policy

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryProofOfDeliveryScheduleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryProofOfDeliveryScheduleEngine(newRegistry())
        val item = newPrimaryProofOfDeliverySchedule("code-825", "label-718")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
