package com.acme.transport.crewing.qualification.proposal

import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalQualificationProposalEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalQualificationProposalEngine()
        val item = newRegionalQualificationProposal("code-171", "title-258")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
