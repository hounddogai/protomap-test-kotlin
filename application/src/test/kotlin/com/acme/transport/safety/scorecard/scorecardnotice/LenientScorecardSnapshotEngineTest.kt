package com.acme.transport.safety.scorecard.scorecardnotice

import kotlin.test.Test
import kotlin.test.assertTrue

class LenientScorecardSnapshotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientScorecardSnapshotEngine()
        val item = newLenientScorecardSnapshot("code-458", "title-59")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
