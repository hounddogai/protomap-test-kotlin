package com.acme.transport.crewing.qualification.assignment

import kotlin.test.Test
import kotlin.test.assertTrue

class QualificationAgreementEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultQualificationAgreementEngine()
        val item = newQualificationAgreement("code-413", "label-628")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
