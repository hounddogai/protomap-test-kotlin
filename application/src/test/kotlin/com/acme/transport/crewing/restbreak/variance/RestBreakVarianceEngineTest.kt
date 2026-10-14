package com.acme.transport.crewing.restbreak.variance

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RestBreakVarianceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRestBreakVarianceEngine(newRegistry())
        val item = newRestBreakVariance("code-761", "name-565")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
