package com.acme.transport.crewing.qualification.qualificationcycle

import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalQualificationCycleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalQualificationCycleEngine()
        val item = newSeasonalQualificationCycle("code-632", "title-613")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
