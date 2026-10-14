package com.acme.transport.crewing.roster.agreement

import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedRosterAgreementEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedRosterAgreementEngine()
        val item = newBatchedRosterAgreement("code-868", "title-218")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
