package com.acme.transport.lastmile.attempt.threshold

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AttemptThresholdEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAttemptThresholdEngine(newRegistry())
        val item = newAttemptThreshold("code-365", "title-255")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
