package com.acme.transport.fleetops.inspection.segment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ManualInspectionGroupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualInspectionGroupEngine(newRegistry())
        val item = newManualInspectionGroup("code-808", "name-2")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
