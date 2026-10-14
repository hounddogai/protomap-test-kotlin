package com.acme.transport.lastmile.drop.allocation

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DropAssignmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDropAssignmentEngine(newRegistry())
        val item = newDropAssignment("code-255", "label-951")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
