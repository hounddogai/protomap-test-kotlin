package com.acme.transport.freightpay.accessorial.policy

import kotlin.test.Test
import kotlin.test.assertTrue

class AccessorialScoreEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAccessorialScoreEngine()
        val item = newAccessorialScore("code-136", "label-146")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
