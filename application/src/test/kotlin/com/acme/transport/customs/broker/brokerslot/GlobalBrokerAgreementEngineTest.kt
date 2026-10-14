package com.acme.transport.customs.broker.brokerslot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalBrokerAgreementEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalBrokerAgreementEngine(newRegistry())
        val item = newGlobalBrokerAgreement("code-797", "label-391")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
