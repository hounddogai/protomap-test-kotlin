package com.acme.transport.terminal.dwell.cycle

import kotlin.test.Test
import kotlin.test.assertTrue

class DwellEntryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDwellEntryEngine()
        val item = newDwellEntry("code-163", "name-139")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
