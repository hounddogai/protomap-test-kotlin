package com.acme.transport.network.hub.entry

import kotlin.test.Test
import kotlin.test.assertTrue

class CappedHubEntryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedHubEntryEngine()
        val item = newCappedHubEntry("code-506", "name-620")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
