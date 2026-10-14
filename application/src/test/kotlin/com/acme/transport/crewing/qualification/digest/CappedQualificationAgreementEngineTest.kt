package com.acme.transport.crewing.qualification.digest

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CappedQualificationAgreementEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedQualificationAgreementEngine(newRegistry())
        val item = newCappedQualificationAgreement("code-54", "name-192")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
