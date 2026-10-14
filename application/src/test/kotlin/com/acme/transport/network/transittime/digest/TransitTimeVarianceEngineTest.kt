package com.acme.transport.network.transittime.digest

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TransitTimeVarianceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTransitTimeVarianceEngine(newRegistry())
        val item = newTransitTimeVariance("code-753", "title-789")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
