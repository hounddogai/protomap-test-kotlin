package com.acme.transport.lastmile.attempt.quota

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AttemptQuotaEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAttemptQuotaEngine(newRegistry())
        val item = newAttemptQuota("code-144", "title-932")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
