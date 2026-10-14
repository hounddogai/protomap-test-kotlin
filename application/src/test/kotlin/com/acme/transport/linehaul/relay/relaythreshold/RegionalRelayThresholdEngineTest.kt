package com.acme.transport.linehaul.relay.relaythreshold

import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalRelayThresholdEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalRelayThresholdEngine()
        val item = newRegionalRelayThreshold("code-612", "name-208")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
