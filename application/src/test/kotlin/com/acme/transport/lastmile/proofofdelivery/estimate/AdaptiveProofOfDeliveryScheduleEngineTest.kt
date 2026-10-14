package com.acme.transport.lastmile.proofofdelivery.estimate

import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveProofOfDeliveryScheduleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveProofOfDeliveryScheduleEngine()
        val item = newAdaptiveProofOfDeliverySchedule("code-212", "name-735")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
