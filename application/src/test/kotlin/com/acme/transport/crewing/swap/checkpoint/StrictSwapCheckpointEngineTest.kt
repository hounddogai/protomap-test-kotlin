package com.acme.transport.crewing.swap.checkpoint

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class StrictSwapCheckpointEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictSwapCheckpointEngine(newRegistry())
        val item = newStrictSwapCheckpoint("code-623", "title-530")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
