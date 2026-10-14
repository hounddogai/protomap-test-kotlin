package com.acme.transport.terminal.appointment.threshold

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AppointmentThresholdEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAppointmentThresholdEngine(newRegistry())
        val item = newAppointmentThreshold("code-19", "title-381")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
