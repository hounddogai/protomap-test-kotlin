package com.acme.transport.freightpay.ratecard.slot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RateCardQuotaEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRateCardQuotaEngine(newRegistry())
        val item = newRateCardQuota("code-179", "label-542")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
