package com.acme.transport.terminal.crossdock.assignment

import kotlin.test.Test
import kotlin.test.assertTrue

class CrossDockEntryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCrossDockEntryEngine()
        val item = newCrossDockEntry("code-347", "title-160")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
