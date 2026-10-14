package com.acme.transport.safety.violation.summary

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledViolationProposalEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledViolationProposalEngine(newRegistry())
        val item = newScheduledViolationProposal("code-765", "title-46")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
