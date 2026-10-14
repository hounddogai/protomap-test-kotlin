package com.acme.transport.lastmile.proofofdelivery.notice

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryProofOfDeliveryNoticeEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryProofOfDeliveryNoticeEngine(newRegistry())
        val item = newPrimaryProofOfDeliveryNotice("code-54", "name-773")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
