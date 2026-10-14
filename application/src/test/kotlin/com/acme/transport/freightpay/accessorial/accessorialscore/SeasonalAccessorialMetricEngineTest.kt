package com.acme.transport.freightpay.accessorial.accessorialscore

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalAccessorialMetricEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalAccessorialMetricEngine(newRegistry())
        val item = newSeasonalAccessorialMetric("code-579", "title-804")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
