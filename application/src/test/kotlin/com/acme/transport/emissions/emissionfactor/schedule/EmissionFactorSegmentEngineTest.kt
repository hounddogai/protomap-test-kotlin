package com.acme.transport.emissions.emissionfactor.schedule

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class EmissionFactorSegmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultEmissionFactorSegmentEngine(newRegistry())
        val item = newEmissionFactorSegment("code-167", "name-928")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
