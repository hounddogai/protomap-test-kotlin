package com.acme.transport.linehaul.relay.checkpoint

import kotlin.test.Test
import kotlin.test.assertTrue

class RelayPreferenceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRelayPreferenceEngine()
        val item = newRelayPreference("code-118", "label-61")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
