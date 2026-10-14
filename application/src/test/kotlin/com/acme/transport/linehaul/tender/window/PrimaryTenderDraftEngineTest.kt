package com.acme.transport.linehaul.tender.window

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryTenderDraftEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryTenderDraftEngine(newRegistry())
        val item = newPrimaryTenderDraft("code-688", "title-649")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
