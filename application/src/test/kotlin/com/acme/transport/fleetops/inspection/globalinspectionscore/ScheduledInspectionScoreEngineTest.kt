package com.acme.transport.fleetops.inspection.globalinspectionscore

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledInspectionScoreEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledInspectionScoreEngine(newRegistry())
        val item = newScheduledInspectionScore("code-572", "name-691")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
