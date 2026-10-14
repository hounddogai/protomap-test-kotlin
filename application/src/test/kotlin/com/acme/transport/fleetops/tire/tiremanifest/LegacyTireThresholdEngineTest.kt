package com.acme.transport.fleetops.tire.tiremanifest

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyTireThresholdEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyTireThresholdEngine(newRegistry())
        val item = newLegacyTireThreshold("code-610", "label-539")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
