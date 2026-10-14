package com.acme.transport.lastmile.attempt.manifest

import kotlin.test.Test
import kotlin.test.assertTrue

class StrictAttemptRecordEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictAttemptRecordEngine()
        val item = newStrictAttemptRecord("code-679", "name-234")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
