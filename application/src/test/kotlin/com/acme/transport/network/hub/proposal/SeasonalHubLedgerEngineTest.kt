package com.acme.transport.network.hub.proposal

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalHubLedgerEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalHubLedgerEngine(newRegistry())
        val item = newSeasonalHubLedger("code-247", "name-604")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
