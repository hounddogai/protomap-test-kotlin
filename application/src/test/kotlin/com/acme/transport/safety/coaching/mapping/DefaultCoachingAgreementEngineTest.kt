package com.acme.transport.safety.coaching.mapping

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultCoachingAgreementEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultCoachingAgreementEngine(newRegistry())
        val item = newDefaultCoachingAgreement("code-874", "label-526")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
