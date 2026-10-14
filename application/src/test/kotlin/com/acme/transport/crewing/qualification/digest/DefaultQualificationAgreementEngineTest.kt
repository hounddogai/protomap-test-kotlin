package com.acme.transport.crewing.qualification.digest

import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultQualificationAgreementEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultQualificationAgreementEngine()
        val item = newDefaultQualificationAgreement("code-922", "title-433")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
