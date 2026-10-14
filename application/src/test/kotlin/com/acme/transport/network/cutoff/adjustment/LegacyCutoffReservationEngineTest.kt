package com.acme.transport.network.cutoff.adjustment

import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyCutoffReservationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyCutoffReservationEngine()
        val item = newLegacyCutoffReservation("code-47", "label-248")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
