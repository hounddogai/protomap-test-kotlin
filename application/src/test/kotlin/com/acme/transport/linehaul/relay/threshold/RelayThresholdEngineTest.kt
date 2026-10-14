package com.acme.transport.linehaul.relay.threshold

import kotlin.test.Test
import kotlin.test.assertTrue

class RelayThresholdEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRelayThresholdEngine()
        val item = newRelayThreshold("code-824", "title-287")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
