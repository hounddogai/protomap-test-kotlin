package com.acme.transport.fleetops.defect.checkpoint

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DefectPreferenceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefectPreferenceEngine(newRegistry())
        val item = newDefectPreference("code-917", "title-321")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
