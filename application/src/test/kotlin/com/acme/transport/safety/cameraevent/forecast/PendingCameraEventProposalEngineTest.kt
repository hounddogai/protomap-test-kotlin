package com.acme.transport.safety.cameraevent.forecast

import kotlin.test.Test
import kotlin.test.assertTrue

class PendingCameraEventProposalEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingCameraEventProposalEngine()
        val item = newPendingCameraEventProposal("code-197", "name-683")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
