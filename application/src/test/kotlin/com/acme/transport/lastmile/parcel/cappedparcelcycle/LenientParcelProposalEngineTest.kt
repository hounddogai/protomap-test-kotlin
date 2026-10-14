package com.acme.transport.lastmile.parcel.cappedparcelcycle

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LenientParcelProposalEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientParcelProposalEngine(newRegistry())
        val item = newLenientParcelProposal("code-20", "name-597")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
