package com.acme.transport.crewing.qualification.plan

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class QualificationProposalEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultQualificationProposalEngine(newRegistry())
        val item = newQualificationProposal("code-607", "name-671")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
