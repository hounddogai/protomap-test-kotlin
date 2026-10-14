package com.acme.transport.network.zone.draft

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyZoneTierEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyZoneTierEngine(newRegistry())
        val item = newLegacyZoneTier("code-810", "name-716")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
