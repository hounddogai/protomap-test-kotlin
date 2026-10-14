package com.acme.transport.network.transittime.timedigest

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedTransitTimeDigestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedTransitTimeDigestEngine(newRegistry())
        val item = newWeightedTransitTimeDigest("code-733", "title-151")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
