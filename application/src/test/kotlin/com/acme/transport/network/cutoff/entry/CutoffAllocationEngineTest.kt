package com.acme.transport.network.cutoff.entry

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CutoffAllocationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCutoffAllocationEngine(newRegistry())
        val item = newCutoffAllocation("code-770", "name-239")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
