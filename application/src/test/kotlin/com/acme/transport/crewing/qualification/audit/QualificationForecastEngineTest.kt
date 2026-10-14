package com.acme.transport.crewing.qualification.audit

import kotlin.test.Test
import kotlin.test.assertTrue

class QualificationForecastEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultQualificationForecastEngine()
        val item = newQualificationForecast("code-733", "name-947")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
