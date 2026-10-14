package com.acme.transport.terminal.appointment.bundle

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AppointmentQuotaEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAppointmentQuotaEngine(newRegistry())
        val item = newAppointmentQuota("code-591", "title-909")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
