package com.acme.transport.lastmile.attempt.window

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultAttemptClaimEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultAttemptClaimEngine(newRegistry())
        val item = newDefaultAttemptClaim("code-252", "title-922")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
