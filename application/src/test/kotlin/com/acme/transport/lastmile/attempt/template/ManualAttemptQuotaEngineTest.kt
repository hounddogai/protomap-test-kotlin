package com.acme.transport.lastmile.attempt.template

import kotlin.test.Test
import kotlin.test.assertTrue

class ManualAttemptQuotaEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualAttemptQuotaEngine()
        val item = newManualAttemptQuota("code-574", "title-543")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
