package com.acme.transport.network.zone.schedule

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledZoneReservationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledZoneReservationEngine(newRegistry())
        val item = newScheduledZoneReservation("code-440", "label-478")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
