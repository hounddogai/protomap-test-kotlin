package com.acme.transport.customs.inspection.inspectionoverride

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledInspectionClaimEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledInspectionClaimEngine(newRegistry())
        val item = newScheduledInspectionClaim("code-555", "name-893")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
