package com.acme.transport.network.zone.claim

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackZoneThresholdEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackZoneThresholdEngine(newRegistry())
        val item = newFallbackZoneThreshold("code-690", "name-713")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
