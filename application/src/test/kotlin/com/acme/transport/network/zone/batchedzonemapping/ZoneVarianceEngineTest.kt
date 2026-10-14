package com.acme.transport.network.zone.batchedzonemapping

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ZoneVarianceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultZoneVarianceEngine(newRegistry())
        val item = newZoneVariance("code-359", "label-497")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
