package com.acme.transport.terminal.appointment.bundle

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultAppointmentManifestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultAppointmentManifestEngine(newRegistry())
        val item = newDefaultAppointmentManifest("code-227", "label-355")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
