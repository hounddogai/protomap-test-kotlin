package com.acme.transport.linehaul.tender.metric

import kotlin.test.Test
import kotlin.test.assertTrue

class PendingTenderScoreEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingTenderScoreEngine()
        val item = newPendingTenderScore("code-644", "name-93")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
