package com.acme.transport.linehaul.capacity.entry

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyCapacityThresholdEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyCapacityThresholdEngine(newRegistry())
        val item = newLegacyCapacityThreshold("code-139", "name-558")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
