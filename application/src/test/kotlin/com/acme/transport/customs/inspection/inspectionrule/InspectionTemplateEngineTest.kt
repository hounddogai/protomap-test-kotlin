package com.acme.transport.customs.inspection.inspectionrule

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class InspectionTemplateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultInspectionTemplateEngine(newRegistry())
        val item = newInspectionTemplate("code-435", "name-893")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
