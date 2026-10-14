package com.acme.transport.network.hub.estimate

import kotlin.test.Test
import kotlin.test.assertTrue

class LenientHubBaselineEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientHubBaselineEngine()
        val item = newLenientHubBaseline("code-415", "name-315")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
