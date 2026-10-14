package com.acme.transport.linehaul.tender.plan

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalTenderDraftEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalTenderDraftEngine(newRegistry())
        val item = newGlobalTenderDraft("code-8", "name-238")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
