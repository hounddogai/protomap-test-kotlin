package com.acme.transport.linehaul.trailer.threshold

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TrailerCheckpointEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTrailerCheckpointEngine(newRegistry())
        val item = newTrailerCheckpoint("code-379", "label-392")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
