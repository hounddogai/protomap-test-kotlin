package com.acme.transport.linehaul.relay.schedule

import kotlin.test.Test
import kotlin.test.assertTrue

class TieredRelayOverrideEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredRelayOverrideEngine()
        val item = newTieredRelayOverride("code-990", "name-694")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
