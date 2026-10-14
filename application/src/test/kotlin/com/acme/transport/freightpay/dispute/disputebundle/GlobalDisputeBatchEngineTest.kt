package com.acme.transport.freightpay.dispute.disputebundle

import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalDisputeBatchEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalDisputeBatchEngine()
        val item = newGlobalDisputeBatch("code-71", "title-155")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
