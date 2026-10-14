package com.acme.transport.emissions.emissionfactor.factorpreference

import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryEmissionFactorReviewEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryEmissionFactorReviewEngine()
        val item = newPrimaryEmissionFactorReview("code-102", "label-735")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
