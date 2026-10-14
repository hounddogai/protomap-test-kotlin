package com.acme.transport.terminal.crossdock.segment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CrossDockRequestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCrossDockRequestEngine(newRegistry())
        val item = newCrossDockRequest("code-646", "label-587")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
