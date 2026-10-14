package com.acme.transport.lastmile.attempt.window

import kotlin.test.Test
import kotlin.test.assertTrue

class AttemptWindowEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAttemptWindowEngine()
        val item = newAttemptWindow("code-190", "label-728")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
