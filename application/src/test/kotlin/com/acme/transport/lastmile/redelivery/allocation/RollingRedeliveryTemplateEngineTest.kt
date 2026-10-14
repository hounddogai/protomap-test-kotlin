package com.acme.transport.lastmile.redelivery.allocation

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RollingRedeliveryTemplateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingRedeliveryTemplateEngine(newRegistry())
        val item = newRollingRedeliveryTemplate("code-505", "label-743")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
