package com.acme.transport.lastmile.proofofdelivery.rollup

import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledProofOfDeliveryDigestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledProofOfDeliveryDigestEngine()
        val item = newScheduledProofOfDeliveryDigest("code-957", "title-618")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
