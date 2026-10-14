package com.acme.transport.terminal.appointment.exception

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryAppointmentQuotaEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryAppointmentQuotaEngine(newRegistry())
        val item = newPrimaryAppointmentQuota("code-348", "name-507")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
