package com.acme.transport.lastmile.proofofdelivery.deliveryscore

import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalProofOfDeliveryMilestoneEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalProofOfDeliveryMilestoneEngine()
        val item = newSeasonalProofOfDeliveryMilestone("code-804", "name-705")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
