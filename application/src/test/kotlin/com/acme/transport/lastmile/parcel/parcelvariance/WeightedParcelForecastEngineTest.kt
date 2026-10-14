package com.acme.transport.lastmile.parcel.parcelvariance

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedParcelForecastEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedParcelForecastEngine(newRegistry())
        val item = newWeightedParcelForecast("code-448", "title-528")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
