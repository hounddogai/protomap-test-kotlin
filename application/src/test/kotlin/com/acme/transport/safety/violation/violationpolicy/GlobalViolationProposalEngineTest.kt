package com.acme.transport.safety.violation.violationpolicy

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalViolationProposalEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalViolationProposalEngine(newRegistry())
        val item = newGlobalViolationProposal("code-597", "name-194")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
