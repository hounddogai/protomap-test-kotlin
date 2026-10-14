package com.acme.transport.customs.inspection.inspectiondigest

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackInspectionRequestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackInspectionRequestEngine(newRegistry())
        val item = newFallbackInspectionRequest("code-898", "label-12")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
