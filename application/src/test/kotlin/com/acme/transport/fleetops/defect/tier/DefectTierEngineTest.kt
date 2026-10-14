package com.acme.transport.fleetops.defect.tier

import kotlin.test.Test
import kotlin.test.assertTrue

class DefectTierEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefectTierEngine()
        val item = newDefectTier("code-648", "name-597")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
