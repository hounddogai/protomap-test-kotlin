package com.acme.transport.lastmile.redelivery.checkpoint

import kotlin.test.Test
import kotlin.test.assertTrue

class RedeliveryAgreementEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRedeliveryAgreementEngine()
        val item = newRedeliveryAgreement("code-219", "title-876")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
