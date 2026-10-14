package com.acme.transport.crewing.restbreak.breakmilestone

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class StrictRestBreakCheckpointEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictRestBreakCheckpointEngine(newRegistry())
        val item = newStrictRestBreakCheckpoint("code-852", "name-458")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
