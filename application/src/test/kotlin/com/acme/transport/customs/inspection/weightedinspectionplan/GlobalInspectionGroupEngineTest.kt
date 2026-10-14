package com.acme.transport.customs.inspection.weightedinspectionplan

import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalInspectionGroupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalInspectionGroupEngine()
        val item = newGlobalInspectionGroup("code-824", "label-437")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
