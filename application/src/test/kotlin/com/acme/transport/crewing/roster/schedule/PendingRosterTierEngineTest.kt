package com.acme.transport.crewing.roster.schedule

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PendingRosterTierEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingRosterTierEngine(newRegistry())
        val item = newPendingRosterTier("code-1", "name-379")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
