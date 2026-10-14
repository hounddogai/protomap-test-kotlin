package com.acme.transport.freightpay.dispute.group

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DisputeQuotaEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDisputeQuotaEngine(newRegistry())
        val item = newDisputeQuota("code-942", "title-606")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
