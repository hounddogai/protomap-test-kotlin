package com.acme.transport.terminal.dwell.bundle

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveDwellEntryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveDwellEntryEngine(newRegistry())
        val item = newAdaptiveDwellEntry("code-214", "name-51")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
