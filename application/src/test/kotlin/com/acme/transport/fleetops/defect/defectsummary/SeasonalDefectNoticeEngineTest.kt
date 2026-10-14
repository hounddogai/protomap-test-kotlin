package com.acme.transport.fleetops.defect.defectsummary

import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalDefectNoticeEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalDefectNoticeEngine()
        val item = newSeasonalDefectNotice("code-96", "name-185")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
