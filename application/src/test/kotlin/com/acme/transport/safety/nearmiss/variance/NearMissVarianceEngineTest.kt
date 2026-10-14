package com.acme.transport.safety.nearmiss.variance

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class NearMissVarianceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultNearMissVarianceEngine(newRegistry())
        val item = newNearMissVariance("code-341", "title-853")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
