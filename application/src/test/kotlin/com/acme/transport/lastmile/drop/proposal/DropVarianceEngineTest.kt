package com.acme.transport.lastmile.drop.proposal

import kotlin.test.Test
import kotlin.test.assertTrue

class DropVarianceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDropVarianceEngine()
        val item = newDropVariance("code-877", "name-405")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
