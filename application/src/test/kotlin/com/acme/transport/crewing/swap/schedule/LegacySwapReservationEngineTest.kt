package com.acme.transport.crewing.swap.schedule

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LegacySwapReservationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacySwapReservationEngine(newRegistry())
        val item = newLegacySwapReservation("code-84", "label-397")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
