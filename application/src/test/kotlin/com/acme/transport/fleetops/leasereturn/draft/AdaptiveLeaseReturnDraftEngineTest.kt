package com.acme.transport.fleetops.leasereturn.draft

import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveLeaseReturnDraftEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveLeaseReturnDraftEngine()
        val item = newAdaptiveLeaseReturnDraft("code-747", "name-98")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
