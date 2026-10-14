package com.acme.transport.terminal.appointment.variance

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryAppointmentMetricEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryAppointmentMetricEngine(newRegistry())
        val item = newPrimaryAppointmentMetric("code-962", "label-975")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
