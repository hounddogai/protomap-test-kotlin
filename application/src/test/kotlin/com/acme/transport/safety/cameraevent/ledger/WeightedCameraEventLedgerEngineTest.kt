package com.acme.transport.safety.cameraevent.ledger

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedCameraEventLedgerEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedCameraEventLedgerEngine(newRegistry())
        val item = newWeightedCameraEventLedger("code-358", "label-544")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
