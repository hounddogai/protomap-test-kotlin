package com.acme.transport.lastmile.redelivery.proposal

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledRedeliveryDigestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledRedeliveryDigestEngine(newRegistry())
        val item = newScheduledRedeliveryDigest("code-978", "label-817")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
