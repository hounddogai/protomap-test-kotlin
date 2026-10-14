package com.acme.transport.freightpay.accessorial.accessorialmetric

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedAccessorialReservationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedAccessorialReservationEngine(newRegistry())
        val item = newBatchedAccessorialReservation("code-480", "title-243")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
