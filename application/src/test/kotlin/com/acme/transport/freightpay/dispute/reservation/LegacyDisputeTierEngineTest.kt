package com.acme.transport.freightpay.dispute.reservation

import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyDisputeTierEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyDisputeTierEngine()
        val item = newLegacyDisputeTier("code-349", "title-201")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
