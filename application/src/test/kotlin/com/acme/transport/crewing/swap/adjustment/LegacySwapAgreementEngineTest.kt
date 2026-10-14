package com.acme.transport.crewing.swap.adjustment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LegacySwapAgreementEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacySwapAgreementEngine(newRegistry())
        val item = newLegacySwapAgreement("code-829", "label-214")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
