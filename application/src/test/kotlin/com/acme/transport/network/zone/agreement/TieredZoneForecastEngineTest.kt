package com.acme.transport.network.zone.agreement

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TieredZoneForecastEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredZoneForecastEngine(newRegistry())
        val item = newTieredZoneForecast("code-966", "label-590")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
