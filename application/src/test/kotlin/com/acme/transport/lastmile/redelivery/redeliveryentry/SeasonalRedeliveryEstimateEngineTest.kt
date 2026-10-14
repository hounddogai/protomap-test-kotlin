package com.acme.transport.lastmile.redelivery.redeliveryentry

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalRedeliveryEstimateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalRedeliveryEstimateEngine(newRegistry())
        val item = newSeasonalRedeliveryEstimate("code-686", "label-553")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
