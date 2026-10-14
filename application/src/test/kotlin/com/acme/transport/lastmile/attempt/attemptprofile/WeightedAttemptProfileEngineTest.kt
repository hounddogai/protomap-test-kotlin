package com.acme.transport.lastmile.attempt.attemptprofile

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedAttemptProfileEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedAttemptProfileEngine(newRegistry())
        val item = newWeightedAttemptProfile("code-488", "title-737")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
