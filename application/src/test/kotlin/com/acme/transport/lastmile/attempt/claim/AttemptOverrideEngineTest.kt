package com.acme.transport.lastmile.attempt.claim

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AttemptOverrideEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAttemptOverrideEngine(newRegistry())
        val item = newAttemptOverride("code-999", "title-259")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
