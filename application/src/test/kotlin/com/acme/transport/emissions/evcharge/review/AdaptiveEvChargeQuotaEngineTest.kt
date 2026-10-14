package com.acme.transport.emissions.evcharge.review

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveEvChargeQuotaEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveEvChargeQuotaEngine(newRegistry())
        val item = newAdaptiveEvChargeQuota("code-949", "label-370")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
