package com.acme.transport.terminal.crossdock.segment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CrossDockAllocationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCrossDockAllocationEngine(newRegistry())
        val item = newCrossDockAllocation("code-345", "title-403")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
