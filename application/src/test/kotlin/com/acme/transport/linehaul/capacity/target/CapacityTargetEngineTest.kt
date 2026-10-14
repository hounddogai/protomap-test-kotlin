package com.acme.transport.linehaul.capacity.target

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CapacityTargetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCapacityTargetEngine(newRegistry())
        val item = newCapacityTarget("code-387", "label-373")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
