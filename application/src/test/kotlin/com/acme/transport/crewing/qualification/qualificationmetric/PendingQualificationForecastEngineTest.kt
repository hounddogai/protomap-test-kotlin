package com.acme.transport.crewing.qualification.qualificationmetric

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PendingQualificationForecastEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingQualificationForecastEngine(newRegistry())
        val item = newPendingQualificationForecast("code-997", "title-646")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
