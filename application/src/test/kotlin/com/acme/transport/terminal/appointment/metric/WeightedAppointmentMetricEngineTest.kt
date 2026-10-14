package com.acme.transport.terminal.appointment.metric

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedAppointmentMetricEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedAppointmentMetricEngine(newRegistry())
        val item = newWeightedAppointmentMetric("code-422", "name-929")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
