package com.acme.transport.lastmile.attempt.metric

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalAttemptCheckpointEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalAttemptCheckpointEngine(newRegistry())
        val item = newGlobalAttemptCheckpoint("code-18", "label-246")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
