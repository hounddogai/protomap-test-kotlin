package com.acme.transport.terminal.dwell.template

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DwellTemplateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDwellTemplateEngine(newRegistry())
        val item = newDwellTemplate("code-536", "name-692")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
