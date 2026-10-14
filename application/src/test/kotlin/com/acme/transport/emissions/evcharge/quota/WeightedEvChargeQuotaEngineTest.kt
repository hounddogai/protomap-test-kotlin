package com.acme.transport.emissions.evcharge.quota

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedEvChargeQuotaEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedEvChargeQuotaEngine(newRegistry())
        val item = newWeightedEvChargeQuota("code-97", "label-474")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
