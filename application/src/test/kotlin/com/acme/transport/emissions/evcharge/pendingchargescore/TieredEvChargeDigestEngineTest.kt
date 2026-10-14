package com.acme.transport.emissions.evcharge.pendingchargescore

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TieredEvChargeDigestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredEvChargeDigestEngine(newRegistry())
        val item = newTieredEvChargeDigest("code-193", "name-549")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
