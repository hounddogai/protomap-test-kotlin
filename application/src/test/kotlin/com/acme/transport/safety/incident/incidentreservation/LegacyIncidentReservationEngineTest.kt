package com.acme.transport.safety.incident.incidentreservation

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyIncidentReservationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyIncidentReservationEngine(newRegistry())
        val item = newLegacyIncidentReservation("code-724", "title-462")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
