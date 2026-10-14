package com.acme.transport.customs.inspection.target

import kotlin.test.Test
import kotlin.test.assertTrue

class ManualInspectionGroupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualInspectionGroupEngine()
        val item = newManualInspectionGroup("code-394", "title-419")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
