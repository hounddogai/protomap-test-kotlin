package com.acme.transport.network.cutoff.regionalcutoffsegment

import kotlin.test.Test
import kotlin.test.assertTrue

class PendingCutoffSegmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingCutoffSegmentEngine()
        val item = newPendingCutoffSegment("code-46", "name-155")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
