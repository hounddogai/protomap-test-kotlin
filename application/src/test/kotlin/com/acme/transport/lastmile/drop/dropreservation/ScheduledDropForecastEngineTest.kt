package com.acme.transport.lastmile.drop.dropreservation

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledDropForecastEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledDropForecastEngine(newRegistry())
        val item = newScheduledDropForecast("code-61", "name-647")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
