package com.acme.transport.lastmile.redelivery.redeliveryentry

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalRedeliveryReservationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalRedeliveryReservationEngine(newRegistry())
        val item = newSeasonalRedeliveryReservation("code-554", "title-470")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
