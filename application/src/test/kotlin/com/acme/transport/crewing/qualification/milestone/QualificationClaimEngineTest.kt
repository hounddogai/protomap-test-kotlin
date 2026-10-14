package com.acme.transport.crewing.qualification.milestone

import kotlin.test.Test
import kotlin.test.assertTrue

class QualificationClaimEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultQualificationClaimEngine()
        val item = newQualificationClaim("code-515", "title-360")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
