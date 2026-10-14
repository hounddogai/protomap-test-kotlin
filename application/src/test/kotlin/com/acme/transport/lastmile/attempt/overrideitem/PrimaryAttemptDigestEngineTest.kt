package com.acme.transport.lastmile.attempt.overrideitem

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryAttemptDigestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryAttemptDigestEngine(newRegistry())
        val item = newPrimaryAttemptDigest("code-661", "label-173")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
