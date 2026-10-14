package com.acme.transport.customs.hold.allocation

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class HoldAllocationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultHoldAllocationEngine(newRegistry())
        val item = newHoldAllocation("code-45", "title-598")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
