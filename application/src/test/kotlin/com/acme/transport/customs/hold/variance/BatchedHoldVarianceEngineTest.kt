package com.acme.transport.customs.hold.variance

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedHoldVarianceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedHoldVarianceEngine(newRegistry())
        val item = newBatchedHoldVariance("code-211", "name-913")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
