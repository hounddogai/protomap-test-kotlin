package com.acme.transport.lastmile.attempt.attemptplan

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AttemptAssignmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAttemptAssignmentEngine(newRegistry())
        val item = newAttemptAssignment("code-154", "title-509")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
