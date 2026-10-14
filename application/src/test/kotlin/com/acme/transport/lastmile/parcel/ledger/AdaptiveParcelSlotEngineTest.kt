package com.acme.transport.lastmile.parcel.ledger

import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveParcelSlotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveParcelSlotEngine()
        val item = newAdaptiveParcelSlot("code-775", "label-2")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
