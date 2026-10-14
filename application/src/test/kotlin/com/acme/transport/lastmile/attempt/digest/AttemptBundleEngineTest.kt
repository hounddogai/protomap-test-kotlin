package com.acme.transport.lastmile.attempt.digest

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AttemptBundleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAttemptBundleEngine(newRegistry())
        val item = newAttemptBundle("code-72", "name-532")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
