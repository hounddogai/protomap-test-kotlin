package com.acme.transport.terminal.dwell.proposal

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalDwellPolicyEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalDwellPolicyEngine(newRegistry())
        val item = newSeasonalDwellPolicy("code-494", "title-404")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
