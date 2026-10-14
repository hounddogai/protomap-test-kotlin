package com.acme.transport.freightpay.dispute.schedule

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackDisputeTierEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackDisputeTierEngine(newRegistry())
        val item = newFallbackDisputeTier("code-873", "title-61")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
