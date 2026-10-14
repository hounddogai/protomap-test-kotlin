package com.acme.transport.safety.coaching.entry

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class StrictCoachingAgreementEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictCoachingAgreementEngine(newRegistry())
        val item = newStrictCoachingAgreement("code-928", "label-270")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
