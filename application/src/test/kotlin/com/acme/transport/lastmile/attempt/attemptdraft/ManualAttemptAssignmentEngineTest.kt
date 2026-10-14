package com.acme.transport.lastmile.attempt.attemptdraft

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ManualAttemptAssignmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualAttemptAssignmentEngine(newRegistry())
        val item = newManualAttemptAssignment("code-416", "title-508")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
