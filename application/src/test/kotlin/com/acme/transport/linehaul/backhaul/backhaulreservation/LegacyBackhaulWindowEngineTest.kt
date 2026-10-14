package com.acme.transport.linehaul.backhaul.backhaulreservation

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyBackhaulWindowEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyBackhaulWindowEngine(newRegistry())
        val item = newLegacyBackhaulWindow("code-956", "title-361")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
