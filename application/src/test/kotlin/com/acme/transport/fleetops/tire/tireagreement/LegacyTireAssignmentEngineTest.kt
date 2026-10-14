package com.acme.transport.fleetops.tire.tireagreement

import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyTireAssignmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyTireAssignmentEngine()
        val item = newLegacyTireAssignment("code-170", "title-543")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
