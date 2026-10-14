package com.acme.transport.network.cutoff.draft

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LenientCutoffSegmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientCutoffSegmentEngine(newRegistry())
        val item = newLenientCutoffSegment("code-264", "label-23")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
