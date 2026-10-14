package com.acme.transport.network.transittime.timebatch

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TieredTransitTimeBatchEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredTransitTimeBatchEngine(newRegistry())
        val item = newTieredTransitTimeBatch("code-371", "name-976")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
