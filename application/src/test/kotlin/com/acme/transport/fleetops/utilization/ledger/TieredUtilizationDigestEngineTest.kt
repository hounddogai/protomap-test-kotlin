package com.acme.transport.fleetops.utilization.ledger

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TieredUtilizationDigestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredUtilizationDigestEngine(newRegistry())
        val item = newTieredUtilizationDigest("code-340", "name-889")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
