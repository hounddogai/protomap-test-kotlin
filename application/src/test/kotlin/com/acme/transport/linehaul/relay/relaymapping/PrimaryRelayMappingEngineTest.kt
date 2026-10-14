package com.acme.transport.linehaul.relay.relaymapping

import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryRelayMappingEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryRelayMappingEngine()
        val item = newPrimaryRelayMapping("code-926", "title-287")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
