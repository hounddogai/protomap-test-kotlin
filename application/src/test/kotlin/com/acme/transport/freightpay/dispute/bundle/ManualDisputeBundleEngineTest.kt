package com.acme.transport.freightpay.dispute.bundle

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ManualDisputeBundleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualDisputeBundleEngine(newRegistry())
        val item = newManualDisputeBundle("code-509", "label-303")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
