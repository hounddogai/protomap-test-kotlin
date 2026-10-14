package com.acme.transport.linehaul.capacity.mapping

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CapacityVarianceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCapacityVarianceEngine(newRegistry())
        val item = newCapacityVariance("code-863", "label-375")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
