package com.acme.transport.network.zone.threshold

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ZoneThresholdEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultZoneThresholdEngine(newRegistry())
        val item = newZoneThreshold("code-681", "label-941")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
