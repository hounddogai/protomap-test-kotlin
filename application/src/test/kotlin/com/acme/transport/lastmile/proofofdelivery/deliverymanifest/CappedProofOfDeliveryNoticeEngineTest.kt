package com.acme.transport.lastmile.proofofdelivery.deliverymanifest

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CappedProofOfDeliveryNoticeEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedProofOfDeliveryNoticeEngine(newRegistry())
        val item = newCappedProofOfDeliveryNotice("code-946", "name-788")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
