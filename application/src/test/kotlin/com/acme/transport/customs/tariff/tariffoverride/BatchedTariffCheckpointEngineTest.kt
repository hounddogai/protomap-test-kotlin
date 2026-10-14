package com.acme.transport.customs.tariff.tariffoverride

import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedTariffCheckpointEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedTariffCheckpointEngine()
        val item = newBatchedTariffCheckpoint("code-298", "label-217")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
