package com.acme.transport.linehaul.tender.slot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedTenderQuotaEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedTenderQuotaEngine(newRegistry())
        val item = newWeightedTenderQuota("code-51", "title-48")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
