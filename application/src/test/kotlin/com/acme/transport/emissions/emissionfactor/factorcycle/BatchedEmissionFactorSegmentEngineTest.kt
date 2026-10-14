package com.acme.transport.emissions.emissionfactor.factorcycle

import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedEmissionFactorSegmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedEmissionFactorSegmentEngine()
        val item = newBatchedEmissionFactorSegment("code-265", "title-56")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
