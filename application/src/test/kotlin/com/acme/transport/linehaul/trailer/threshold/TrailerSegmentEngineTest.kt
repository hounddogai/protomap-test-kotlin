package com.acme.transport.linehaul.trailer.threshold

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TrailerSegmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTrailerSegmentEngine(newRegistry())
        val item = newTrailerSegment("code-348", "title-677")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
