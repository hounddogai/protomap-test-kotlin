package com.acme.transport.terminal.dockdoor.rollingdoorsnapshot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryDockDoorTemplateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryDockDoorTemplateEngine(newRegistry())
        val item = newPrimaryDockDoorTemplate("code-807", "title-411")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
