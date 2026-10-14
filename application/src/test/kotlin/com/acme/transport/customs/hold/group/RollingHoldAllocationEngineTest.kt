package com.acme.transport.customs.hold.group

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RollingHoldAllocationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingHoldAllocationEngine(newRegistry())
        val item = newRollingHoldAllocation("code-420", "title-896")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
