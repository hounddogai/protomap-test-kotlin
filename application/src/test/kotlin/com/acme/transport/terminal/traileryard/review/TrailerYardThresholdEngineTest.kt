package com.acme.transport.terminal.traileryard.review

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TrailerYardThresholdEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTrailerYardThresholdEngine(newRegistry())
        val item = newTrailerYardThreshold("code-702", "title-373")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
