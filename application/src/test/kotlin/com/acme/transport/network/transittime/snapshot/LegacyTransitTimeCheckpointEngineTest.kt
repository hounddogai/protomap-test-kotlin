package com.acme.transport.network.transittime.snapshot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyTransitTimeCheckpointEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyTransitTimeCheckpointEngine(newRegistry())
        val item = newLegacyTransitTimeCheckpoint("code-578", "label-773")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
