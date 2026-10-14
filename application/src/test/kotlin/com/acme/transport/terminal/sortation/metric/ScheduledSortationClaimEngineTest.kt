package com.acme.transport.terminal.sortation.metric

import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledSortationClaimEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledSortationClaimEngine()
        val item = newScheduledSortationClaim("code-19", "label-198")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
