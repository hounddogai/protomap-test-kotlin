package com.acme.transport.lastmile.attempt.ledger

import kotlin.test.Test
import kotlin.test.assertTrue

class AttemptSnapshotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAttemptSnapshotEngine()
        val item = newAttemptSnapshot("code-219", "title-131")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
