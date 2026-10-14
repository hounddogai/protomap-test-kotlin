package com.acme.transport.terminal.dwell.summary

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LenientDwellCheckpointEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientDwellCheckpointEngine(newRegistry())
        val item = newLenientDwellCheckpoint("code-950", "label-423")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
