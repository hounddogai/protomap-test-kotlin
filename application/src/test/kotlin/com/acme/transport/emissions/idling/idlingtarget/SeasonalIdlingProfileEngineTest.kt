package com.acme.transport.emissions.idling.idlingtarget

import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalIdlingProfileEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalIdlingProfileEngine()
        val item = newSeasonalIdlingProfile("code-544", "label-242")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
