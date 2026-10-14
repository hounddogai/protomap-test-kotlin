package com.acme.transport.terminal.crossdock.regionaldockclaim

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedCrossDockClaimEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedCrossDockClaimEngine(newRegistry())
        val item = newWeightedCrossDockClaim("code-564", "title-418")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
