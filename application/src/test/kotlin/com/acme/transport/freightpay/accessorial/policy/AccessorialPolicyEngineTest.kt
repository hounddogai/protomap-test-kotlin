package com.acme.transport.freightpay.accessorial.policy

import kotlin.test.Test
import kotlin.test.assertTrue

class AccessorialPolicyEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAccessorialPolicyEngine()
        val item = newAccessorialPolicy("code-563", "label-647")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
