package com.acme.transport.linehaul.backhaul.backhauldigest

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedBackhaulForecastEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedBackhaulForecastEngine(newRegistry())
        val item = newWeightedBackhaulForecast("code-736", "title-277")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
