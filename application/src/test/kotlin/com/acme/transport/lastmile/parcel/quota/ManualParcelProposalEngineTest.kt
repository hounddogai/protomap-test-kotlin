package com.acme.transport.lastmile.parcel.quota

import kotlin.test.Test
import kotlin.test.assertTrue

class ManualParcelProposalEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualParcelProposalEngine()
        val item = newManualParcelProposal("code-884", "label-413")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
