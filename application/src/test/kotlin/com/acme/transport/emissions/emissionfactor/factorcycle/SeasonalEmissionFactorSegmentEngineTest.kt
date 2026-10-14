package com.acme.transport.emissions.emissionfactor.factorcycle

import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalEmissionFactorSegmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalEmissionFactorSegmentEngine()
        val item = newSeasonalEmissionFactorSegment("code-475", "name-918")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
