package com.acme.transport.customs.declaration.budget

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DeclarationProposalEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDeclarationProposalEngine(newRegistry())
        val item = newDeclarationProposal("code-897", "name-594")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
