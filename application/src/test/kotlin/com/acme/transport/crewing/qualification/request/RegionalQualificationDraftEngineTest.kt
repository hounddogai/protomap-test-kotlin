package com.acme.transport.crewing.qualification.request

import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalQualificationDraftEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalQualificationDraftEngine()
        val item = newRegionalQualificationDraft("code-522", "name-550")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
