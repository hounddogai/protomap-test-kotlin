package com.acme.transport.emissions.fuelburn.metric

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryFuelBurnMetricEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryFuelBurnMetricEngine(newRegistry())
        val item = newPrimaryFuelBurnMetric("code-283", "label-590")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
