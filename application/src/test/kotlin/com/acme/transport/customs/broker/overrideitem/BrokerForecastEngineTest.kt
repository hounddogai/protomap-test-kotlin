package com.acme.transport.customs.broker.overrideitem

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class BrokerForecastEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBrokerForecastEngine(newRegistry())
        val item = newBrokerForecast("code-719", "name-620")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
