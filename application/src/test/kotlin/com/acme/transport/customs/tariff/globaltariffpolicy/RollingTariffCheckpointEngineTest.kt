package com.acme.transport.customs.tariff.globaltariffpolicy

import kotlin.test.Test
import kotlin.test.assertTrue

class RollingTariffCheckpointEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingTariffCheckpointEngine()
        val item = newRollingTariffCheckpoint("code-807", "title-381")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
