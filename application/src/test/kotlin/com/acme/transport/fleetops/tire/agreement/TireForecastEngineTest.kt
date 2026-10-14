package com.acme.transport.fleetops.tire.agreement

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TireForecastEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTireForecastEngine(newRegistry())
        val item = newTireForecast("code-218", "label-81")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
