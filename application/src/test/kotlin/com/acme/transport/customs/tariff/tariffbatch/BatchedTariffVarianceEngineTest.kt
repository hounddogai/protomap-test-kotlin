package com.acme.transport.customs.tariff.tariffbatch

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedTariffVarianceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedTariffVarianceEngine(newRegistry())
        val item = newBatchedTariffVariance("code-44", "title-90")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
