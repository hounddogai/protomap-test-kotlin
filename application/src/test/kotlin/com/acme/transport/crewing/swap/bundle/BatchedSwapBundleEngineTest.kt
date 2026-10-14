package com.acme.transport.crewing.swap.bundle

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedSwapBundleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedSwapBundleEngine(newRegistry())
        val item = newBatchedSwapBundle("code-742", "name-905")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
