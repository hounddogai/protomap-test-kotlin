package com.acme.transport.fleetops.inspection.quota

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class InspectionQuotaEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultInspectionQuotaEngine(newRegistry())
        val item = newInspectionQuota("code-264", "label-198")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
