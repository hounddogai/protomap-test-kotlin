package com.acme.transport.terminal.dockdoor.template

import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalDockDoorTemplateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalDockDoorTemplateEngine()
        val item = newGlobalDockDoorTemplate("code-790", "name-360")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
