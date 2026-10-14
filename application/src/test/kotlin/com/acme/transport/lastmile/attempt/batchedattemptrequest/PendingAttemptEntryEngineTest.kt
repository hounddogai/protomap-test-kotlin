package com.acme.transport.lastmile.attempt.batchedattemptrequest

import kotlin.test.Test
import kotlin.test.assertTrue

class PendingAttemptEntryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingAttemptEntryEngine()
        val item = newPendingAttemptEntry("code-642", "name-548")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
