package com.acme.transport.emissions.emissionfactor.draft

import kotlin.test.Test
import kotlin.test.assertTrue

class EmissionFactorDraftEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultEmissionFactorDraftEngine()
        val item = newEmissionFactorDraft("code-389", "label-343")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
