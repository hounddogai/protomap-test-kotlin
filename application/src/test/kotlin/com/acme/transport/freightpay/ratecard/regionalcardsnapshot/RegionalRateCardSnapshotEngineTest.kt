package com.acme.transport.freightpay.ratecard.regionalcardsnapshot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalRateCardSnapshotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalRateCardSnapshotEngine(newRegistry())
        val item = newRegionalRateCardSnapshot("code-153", "name-15")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
