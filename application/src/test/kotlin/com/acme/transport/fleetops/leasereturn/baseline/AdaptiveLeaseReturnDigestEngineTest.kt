package com.acme.transport.fleetops.leasereturn.baseline

import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveLeaseReturnDigestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveLeaseReturnDigestEngine()
        val item = newAdaptiveLeaseReturnDigest("code-994", "title-853")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
