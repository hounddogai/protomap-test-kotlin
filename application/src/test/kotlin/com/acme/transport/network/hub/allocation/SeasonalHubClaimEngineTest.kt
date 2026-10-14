package com.acme.transport.network.hub.allocation

import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalHubClaimEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalHubClaimEngine()
        val item = newSeasonalHubClaim("code-507", "title-385")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
