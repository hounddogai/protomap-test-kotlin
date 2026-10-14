package com.acme.transport.terminal.traileryard.tier

import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedTrailerYardSnapshotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedTrailerYardSnapshotEngine()
        val item = newWeightedTrailerYardSnapshot("code-501", "title-483")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
