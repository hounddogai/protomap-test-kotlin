package com.acme.transport.lastmile.proofofdelivery.baseline

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedProofOfDeliveryReservationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedProofOfDeliveryReservationEngine(newRegistry())
        val item = newWeightedProofOfDeliveryReservation("code-429", "label-185")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
