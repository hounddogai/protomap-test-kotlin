package com.acme.transport.lastmile.attempt.attemptclaim

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CappedAttemptRecordEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedAttemptRecordEngine(newRegistry())
        val item = newCappedAttemptRecord("code-728", "name-231")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
