package com.acme.transport.freightpay.dispute.digest

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DisputeDigestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDisputeDigestEngine(newRegistry())
        val item = newDisputeDigest("code-540", "title-579")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
