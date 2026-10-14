package com.acme.transport.linehaul.backhaul.batch

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class BackhaulProfileEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBackhaulProfileEngine(newRegistry())
        val item = newBackhaulProfile("code-18", "label-974")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
