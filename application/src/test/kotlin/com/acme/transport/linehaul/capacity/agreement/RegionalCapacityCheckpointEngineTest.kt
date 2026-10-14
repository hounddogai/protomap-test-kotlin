package com.acme.transport.linehaul.capacity.agreement

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalCapacityCheckpointEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalCapacityCheckpointEngine(newRegistry())
        val item = newRegionalCapacityCheckpoint("code-9", "name-927")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
