package com.acme.transport.network.servicearea.areabatch

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalServiceAreaEstimateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalServiceAreaEstimateEngine(newRegistry())
        val item = newRegionalServiceAreaEstimate("code-347", "label-300")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
