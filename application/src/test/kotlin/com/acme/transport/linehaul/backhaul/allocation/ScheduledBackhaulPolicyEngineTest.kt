package com.acme.transport.linehaul.backhaul.allocation

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledBackhaulPolicyEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledBackhaulPolicyEngine(newRegistry())
        val item = newScheduledBackhaulPolicy("code-412", "title-280")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
