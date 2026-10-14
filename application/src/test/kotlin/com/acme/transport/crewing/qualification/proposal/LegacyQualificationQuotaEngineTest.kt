package com.acme.transport.crewing.qualification.proposal

import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyQualificationQuotaEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyQualificationQuotaEngine()
        val item = newLegacyQualificationQuota("code-897", "title-122")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
