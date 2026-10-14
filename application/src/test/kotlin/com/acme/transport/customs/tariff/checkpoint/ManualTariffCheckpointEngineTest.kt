package com.acme.transport.customs.tariff.checkpoint

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ManualTariffCheckpointEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualTariffCheckpointEngine(newRegistry())
        val item = newManualTariffCheckpoint("code-636", "title-542")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
