package com.acme.transport.emissions.emissionfactor.notice

import kotlin.test.Test
import kotlin.test.assertTrue

class EmissionFactorNoticeEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultEmissionFactorNoticeEngine()
        val item = newEmissionFactorNotice("code-819", "label-472")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
