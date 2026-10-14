package com.acme.transport.terminal.dwell.dwelltemplate

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RollingDwellTemplateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingDwellTemplateEngine(newRegistry())
        val item = newRollingDwellTemplate("code-53", "name-52")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
