package com.acme.transport.emissions.emissionfactor.factormilestone

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CappedEmissionFactorDraftEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedEmissionFactorDraftEngine(newRegistry())
        val item = newCappedEmissionFactorDraft("code-616", "label-668")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
