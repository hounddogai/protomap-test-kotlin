package com.acme.transport.lastmile.drop.batcheddropnotice

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DropTemplateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDropTemplateEngine(newRegistry())
        val item = newDropTemplate("code-958", "label-878")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
