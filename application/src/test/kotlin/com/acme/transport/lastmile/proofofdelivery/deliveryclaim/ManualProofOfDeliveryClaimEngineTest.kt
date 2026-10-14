package com.acme.transport.lastmile.proofofdelivery.deliveryclaim

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ManualProofOfDeliveryClaimEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualProofOfDeliveryClaimEngine(newRegistry())
        val item = newManualProofOfDeliveryClaim("code-168", "label-549")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
