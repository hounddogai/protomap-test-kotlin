package com.acme.transport.crewing.swap.score

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class SwapReservationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSwapReservationEngine(newRegistry())
        val item = newSwapReservation("code-763", "label-637")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
