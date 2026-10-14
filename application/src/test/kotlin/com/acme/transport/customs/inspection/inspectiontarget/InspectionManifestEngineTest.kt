package com.acme.transport.customs.inspection.inspectiontarget

import kotlin.test.Test
import kotlin.test.assertTrue

class InspectionManifestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultInspectionManifestEngine()
        val item = newInspectionManifest("code-391", "name-84")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
