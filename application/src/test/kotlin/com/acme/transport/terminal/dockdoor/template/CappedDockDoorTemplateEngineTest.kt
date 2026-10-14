package com.acme.transport.terminal.dockdoor.template

import kotlin.test.Test
import kotlin.test.assertTrue

class CappedDockDoorTemplateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedDockDoorTemplateEngine()
        val item = newCappedDockDoorTemplate("code-499", "label-16")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
