package com.acme.transport.fleetops.defect.bundle

import kotlin.test.Test
import kotlin.test.assertTrue

class DefectBundleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefectBundleEngine()
        val item = newDefectBundle("code-454", "name-147")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
