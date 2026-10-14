package com.acme.transport.linehaul.backhaul.backhaulvariance

import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultBackhaulVarianceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultBackhaulVarianceEngine()
        val item = newDefaultBackhaulVariance("code-764", "name-3")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
