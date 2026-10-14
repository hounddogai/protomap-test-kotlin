package com.acme.transport.terminal.appointment.rollup

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AppointmentMetricEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAppointmentMetricEngine(newRegistry())
        val item = newAppointmentMetric("code-519", "label-683")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
