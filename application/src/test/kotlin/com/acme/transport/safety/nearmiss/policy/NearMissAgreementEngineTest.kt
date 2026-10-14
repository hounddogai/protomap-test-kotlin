package com.acme.transport.safety.nearmiss.policy

import kotlin.test.Test
import kotlin.test.assertTrue

class NearMissAgreementEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultNearMissAgreementEngine()
        val item = newNearMissAgreement("code-34", "name-30")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
