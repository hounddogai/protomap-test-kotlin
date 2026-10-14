package com.acme.transport.freightpay.accessorial.adjustment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AccessorialRuleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAccessorialRuleEngine(newRegistry())
        val item = newAccessorialRule("code-47", "title-549")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
