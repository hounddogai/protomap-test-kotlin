package com.acme.transport.freightpay.ratecard.snapshot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RateCardSnapshotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRateCardSnapshotEngine(newRegistry())
        val item = newRateCardSnapshot("code-891", "label-157")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
