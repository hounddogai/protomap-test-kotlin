package com.acme.transport.crewing.qualification.qualificationcycle

import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalQualificationMappingEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalQualificationMappingEngine()
        val item = newSeasonalQualificationMapping("code-407", "title-445")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
