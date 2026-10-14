package com.acme.transport.emissions.evcharge.overrideitem

import kotlin.test.Test
import kotlin.test.assertTrue

class TieredEvChargeCheckpointEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredEvChargeCheckpointEngine()
        val item = newTieredEvChargeCheckpoint("code-135", "name-202")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
