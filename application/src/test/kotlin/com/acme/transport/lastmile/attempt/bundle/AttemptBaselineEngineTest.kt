package com.acme.transport.lastmile.attempt.bundle

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AttemptBaselineEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAttemptBaselineEngine(newRegistry())
        val item = newAttemptBaseline("code-551", "title-262")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
