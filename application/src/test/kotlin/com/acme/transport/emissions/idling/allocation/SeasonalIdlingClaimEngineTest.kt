package com.acme.transport.emissions.idling.allocation

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalIdlingClaimEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalIdlingClaimEngine(newRegistry())
        val item = newSeasonalIdlingClaim("code-570", "label-170")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
