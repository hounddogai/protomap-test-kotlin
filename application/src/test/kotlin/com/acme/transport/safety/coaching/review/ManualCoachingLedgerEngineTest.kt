package com.acme.transport.safety.coaching.review

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ManualCoachingLedgerEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualCoachingLedgerEngine(newRegistry())
        val item = newManualCoachingLedger("code-376", "title-586")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
