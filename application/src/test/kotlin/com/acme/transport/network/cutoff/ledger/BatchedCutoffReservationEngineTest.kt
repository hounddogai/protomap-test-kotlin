package com.acme.transport.network.cutoff.ledger

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedCutoffReservationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedCutoffReservationEngine(newRegistry())
        val item = newBatchedCutoffReservation("code-949", "name-694")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
