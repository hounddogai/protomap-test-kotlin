package com.acme.transport.freightpay.dispute.variance

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalDisputeProfileEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalDisputeProfileEngine(newRegistry())
        val item = newGlobalDisputeProfile("code-773", "title-802")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
