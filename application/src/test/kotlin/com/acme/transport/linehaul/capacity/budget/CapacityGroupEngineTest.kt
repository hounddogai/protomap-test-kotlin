package com.acme.transport.linehaul.capacity.budget

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CapacityGroupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCapacityGroupEngine(newRegistry())
        val item = newCapacityGroup("code-535", "label-196")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
