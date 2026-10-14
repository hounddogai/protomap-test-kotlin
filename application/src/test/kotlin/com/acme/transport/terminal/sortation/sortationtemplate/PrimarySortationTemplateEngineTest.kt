package com.acme.transport.terminal.sortation.sortationtemplate

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PrimarySortationTemplateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimarySortationTemplateEngine(newRegistry())
        val item = newPrimarySortationTemplate("code-599", "title-620")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
