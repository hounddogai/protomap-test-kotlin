package com.acme.transport.customs.inspection.audit

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalInspectionEstimateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalInspectionEstimateEngine(newRegistry())
        val item = newGlobalInspectionEstimate("code-106", "title-401")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
