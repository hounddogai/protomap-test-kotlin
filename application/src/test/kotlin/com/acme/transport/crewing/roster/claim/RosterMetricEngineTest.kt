package com.acme.transport.crewing.roster.claim

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RosterMetricEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRosterMetricEngine(newRegistry())
        val item = newRosterMetric("code-566", "name-753")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
