package com.acme.transport.crewing.restbreak.breakmapping

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyRestBreakAgreementEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyRestBreakAgreementEngine(newRegistry())
        val item = newLegacyRestBreakAgreement("code-13", "label-980")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
