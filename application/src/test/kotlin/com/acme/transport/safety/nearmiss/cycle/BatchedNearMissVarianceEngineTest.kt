package com.acme.transport.safety.nearmiss.cycle

import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedNearMissVarianceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedNearMissVarianceEngine()
        val item = newBatchedNearMissVariance("code-443", "title-808")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
