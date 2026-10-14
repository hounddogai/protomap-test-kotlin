package com.acme.transport.linehaul.relay.mapping

import kotlin.test.Test
import kotlin.test.assertTrue

class RelayMappingEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRelayMappingEngine()
        val item = newRelayMapping("code-781", "label-927")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
