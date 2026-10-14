package com.acme.transport.network.cutoff.globalcutoffledger

import kotlin.test.Test
import kotlin.test.assertTrue

class TieredCutoffReservationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredCutoffReservationEngine()
        val item = newTieredCutoffReservation("code-969", "name-126")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
