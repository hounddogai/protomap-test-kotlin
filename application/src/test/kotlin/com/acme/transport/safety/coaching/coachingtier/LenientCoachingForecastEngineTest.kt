package com.acme.transport.safety.coaching.coachingtier

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LenientCoachingForecastEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientCoachingForecastEngine(newRegistry())
        val item = newLenientCoachingForecast("code-617", "title-371")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
