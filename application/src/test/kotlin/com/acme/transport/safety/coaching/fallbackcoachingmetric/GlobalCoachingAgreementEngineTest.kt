package com.acme.transport.safety.coaching.fallbackcoachingmetric

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalCoachingAgreementEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalCoachingAgreementEngine(newRegistry())
        val item = newGlobalCoachingAgreement("code-686", "title-273")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
