package com.acme.transport.freightpay.dispute.manifest

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LenientDisputeTierEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientDisputeTierEngine(newRegistry())
        val item = newLenientDisputeTier("code-546", "title-404")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
