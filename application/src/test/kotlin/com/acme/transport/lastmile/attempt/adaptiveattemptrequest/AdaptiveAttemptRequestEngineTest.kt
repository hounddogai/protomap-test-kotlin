package com.acme.transport.lastmile.attempt.adaptiveattemptrequest

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveAttemptRequestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveAttemptRequestEngine(newRegistry())
        val item = newAdaptiveAttemptRequest("code-854", "label-82")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
