package com.acme.transport.lastmile.proofofdelivery.deliveryrollup

import kotlin.test.Test
import kotlin.test.assertTrue

class RollingProofOfDeliveryQuotaEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingProofOfDeliveryQuotaEngine()
        val item = newRollingProofOfDeliveryQuota("code-460", "title-532")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
