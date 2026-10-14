package com.acme.transport.network.hub.rollinghubentry

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalHubEntryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalHubEntryEngine(newRegistry())
        val item = newSeasonalHubEntry("code-986", "name-172")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
