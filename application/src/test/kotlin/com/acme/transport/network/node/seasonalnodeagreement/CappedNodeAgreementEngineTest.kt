package com.acme.transport.network.node.seasonalnodeagreement

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CappedNodeAgreementEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedNodeAgreementEngine(newRegistry())
        val item = newCappedNodeAgreement("code-326", "title-60")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
