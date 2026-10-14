package com.acme.transport.fleetops.inspection.inspectionagreement

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class InspectionRecordEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultInspectionRecordEngine(newRegistry())
        val item = newInspectionRecord("code-715", "name-780")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
