package com.acme.transport.network.node.ledger

import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryNodeVarianceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryNodeVarianceEngine()
        val item = newPrimaryNodeVariance("code-81", "title-838")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
