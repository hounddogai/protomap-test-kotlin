package com.acme.transport.freightpay.dispute.request

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyDisputeProfileEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyDisputeProfileEngine(newRegistry())
        val item = newLegacyDisputeProfile("code-643", "label-244")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
