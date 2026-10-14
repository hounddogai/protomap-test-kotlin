package com.acme.transport.crewing.restbreak.checkpoint

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledRestBreakCheckpointEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledRestBreakCheckpointEngine(newRegistry())
        val item = newScheduledRestBreakCheckpoint("code-757", "name-822")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
