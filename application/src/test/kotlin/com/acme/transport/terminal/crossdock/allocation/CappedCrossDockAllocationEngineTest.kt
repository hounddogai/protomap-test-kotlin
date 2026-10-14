package com.acme.transport.terminal.crossdock.allocation

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CappedCrossDockAllocationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedCrossDockAllocationEngine(newRegistry())
        val item = newCappedCrossDockAllocation("code-889", "label-382")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
