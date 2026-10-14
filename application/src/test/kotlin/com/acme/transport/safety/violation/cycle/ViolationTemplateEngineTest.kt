package com.acme.transport.safety.violation.cycle

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ViolationTemplateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultViolationTemplateEngine(newRegistry())
        val item = newViolationTemplate("code-896", "label-353")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
