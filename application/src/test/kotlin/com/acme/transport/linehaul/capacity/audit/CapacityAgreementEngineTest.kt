package com.acme.transport.linehaul.capacity.audit

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CapacityAgreementEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCapacityAgreementEngine(newRegistry())
        val item = newCapacityAgreement("code-940", "title-529")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
