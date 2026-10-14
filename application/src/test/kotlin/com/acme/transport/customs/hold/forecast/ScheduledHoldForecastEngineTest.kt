package com.acme.transport.customs.hold.forecast

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledHoldForecastEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledHoldForecastEngine(newRegistry())
        val item = newScheduledHoldForecast("code-264", "label-328")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
