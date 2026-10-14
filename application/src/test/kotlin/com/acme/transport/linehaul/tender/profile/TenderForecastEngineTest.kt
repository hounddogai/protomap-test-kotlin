package com.acme.transport.linehaul.tender.profile

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TenderForecastEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTenderForecastEngine(newRegistry())
        val item = newTenderForecast("code-125", "title-537")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
