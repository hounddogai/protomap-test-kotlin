package com.acme.transport.freightpay.accessorial.scheduledaccessorialmetric

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledAccessorialDraftEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledAccessorialDraftEngine(newRegistry())
        val item = newScheduledAccessorialDraft("code-562", "name-435")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
