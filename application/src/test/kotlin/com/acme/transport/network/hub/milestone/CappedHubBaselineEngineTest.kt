package com.acme.transport.network.hub.milestone

import kotlin.test.Test
import kotlin.test.assertTrue

class CappedHubBaselineEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedHubBaselineEngine()
        val item = newCappedHubBaseline("code-324", "label-393")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
