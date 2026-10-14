package com.acme.transport.network.zone.agreement

import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackZoneSlotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackZoneSlotEngine()
        val item = newFallbackZoneSlot("code-918", "title-281")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
