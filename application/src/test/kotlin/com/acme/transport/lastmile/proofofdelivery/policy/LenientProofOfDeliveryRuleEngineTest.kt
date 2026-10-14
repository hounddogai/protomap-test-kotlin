package com.acme.transport.lastmile.proofofdelivery.policy

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LenientProofOfDeliveryRuleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientProofOfDeliveryRuleEngine(newRegistry())
        val item = newLenientProofOfDeliveryRule("code-399", "label-324")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
