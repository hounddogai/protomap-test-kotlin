package com.acme.transport.customs.tariff.tariffschedule

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TariffReservationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTariffReservationEngine(newRegistry())
        val item = newTariffReservation("code-438", "name-841")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
