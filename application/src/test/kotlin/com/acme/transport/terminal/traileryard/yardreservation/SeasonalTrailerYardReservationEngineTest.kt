package com.acme.transport.terminal.traileryard.yardreservation

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalTrailerYardReservationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalTrailerYardReservationEngine(newRegistry())
        val item = newSeasonalTrailerYardReservation("code-231", "label-360")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
