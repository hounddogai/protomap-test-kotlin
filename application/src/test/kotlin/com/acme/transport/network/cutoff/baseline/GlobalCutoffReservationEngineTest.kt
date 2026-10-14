package com.acme.transport.network.cutoff.baseline

import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalCutoffReservationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalCutoffReservationEngine()
        val item = newGlobalCutoffReservation("code-971", "name-113")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
