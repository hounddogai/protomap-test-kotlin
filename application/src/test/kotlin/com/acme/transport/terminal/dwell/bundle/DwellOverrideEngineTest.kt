package com.acme.transport.terminal.dwell.bundle

import kotlin.test.Test
import kotlin.test.assertTrue

class DwellOverrideEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDwellOverrideEngine()
        val item = newDwellOverride("code-536", "title-333")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
