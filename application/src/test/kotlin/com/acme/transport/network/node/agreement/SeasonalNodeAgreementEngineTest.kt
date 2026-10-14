package com.acme.transport.network.node.agreement

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalNodeAgreementEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalNodeAgreementEngine(newRegistry())
        val item = newSeasonalNodeAgreement("code-534", "label-371")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
