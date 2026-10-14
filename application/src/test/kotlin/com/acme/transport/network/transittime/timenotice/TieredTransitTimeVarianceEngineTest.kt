package com.acme.transport.network.transittime.timenotice

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TieredTransitTimeVarianceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredTransitTimeVarianceEngine(newRegistry())
        val item = newTieredTransitTimeVariance("code-85", "name-576")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
