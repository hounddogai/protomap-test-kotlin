package com.acme.transport.safety.nearmiss.batch

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LenientNearMissAgreementEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientNearMissAgreementEngine(newRegistry())
        val item = newLenientNearMissAgreement("code-379", "name-429")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
