package com.acme.transport.network.node.ledger

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackNodeEstimateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackNodeEstimateEngine(newRegistry())
        val item = newFallbackNodeEstimate("code-611", "name-632")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
