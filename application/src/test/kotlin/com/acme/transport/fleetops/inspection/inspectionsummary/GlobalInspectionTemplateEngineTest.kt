package com.acme.transport.fleetops.inspection.inspectionsummary

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalInspectionTemplateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalInspectionTemplateEngine(newRegistry())
        val item = newGlobalInspectionTemplate("code-77", "name-919")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
