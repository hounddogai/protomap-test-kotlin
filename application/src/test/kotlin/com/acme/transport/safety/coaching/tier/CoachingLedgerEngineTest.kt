package com.acme.transport.safety.coaching.tier

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CoachingLedgerEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCoachingLedgerEngine(newRegistry())
        val item = newCoachingLedger("code-139", "title-875")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
