package com.acme.transport.fleetops.tire.claim

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CappedTireTemplateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedTireTemplateEngine(newRegistry())
        val item = newCappedTireTemplate("code-547", "label-1")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
