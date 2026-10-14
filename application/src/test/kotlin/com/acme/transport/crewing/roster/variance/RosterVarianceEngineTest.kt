package com.acme.transport.crewing.roster.variance

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RosterVarianceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRosterVarianceEngine(newRegistry())
        val item = newRosterVariance("code-872", "label-875")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
