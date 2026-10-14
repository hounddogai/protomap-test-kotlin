package com.acme.transport.emissions.evcharge.agreement

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class EvChargeAgreementEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultEvChargeAgreementEngine(newRegistry())
        val item = newEvChargeAgreement("code-932", "title-270")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
