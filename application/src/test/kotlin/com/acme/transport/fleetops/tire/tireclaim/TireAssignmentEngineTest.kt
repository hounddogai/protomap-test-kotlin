package com.acme.transport.fleetops.tire.tireclaim

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TireAssignmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTireAssignmentEngine(newRegistry())
        val item = newTireAssignment("code-35", "title-403")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
