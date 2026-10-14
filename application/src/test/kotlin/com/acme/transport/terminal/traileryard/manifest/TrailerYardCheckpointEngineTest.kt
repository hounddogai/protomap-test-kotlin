package com.acme.transport.terminal.traileryard.manifest

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TrailerYardCheckpointEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTrailerYardCheckpointEngine(newRegistry())
        val item = newTrailerYardCheckpoint("code-272", "label-141")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
