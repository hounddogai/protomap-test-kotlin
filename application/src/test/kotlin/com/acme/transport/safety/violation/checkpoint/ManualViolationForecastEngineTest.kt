package com.acme.transport.safety.violation.checkpoint

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ManualViolationForecastEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualViolationForecastEngine(newRegistry())
        val item = newManualViolationForecast("code-797", "label-911")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
