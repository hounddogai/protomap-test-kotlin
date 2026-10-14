package com.acme.transport.lastmile.attempt.threshold

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AttemptCheckpointEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAttemptCheckpointEngine(newRegistry())
        val item = newAttemptCheckpoint("code-153", "title-995")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
