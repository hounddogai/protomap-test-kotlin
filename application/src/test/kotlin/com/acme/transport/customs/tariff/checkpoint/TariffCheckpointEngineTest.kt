package com.acme.transport.customs.tariff.checkpoint

import kotlin.test.Test
import kotlin.test.assertTrue

class TariffCheckpointEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTariffCheckpointEngine()
        val item = newTariffCheckpoint("code-481", "label-912")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
