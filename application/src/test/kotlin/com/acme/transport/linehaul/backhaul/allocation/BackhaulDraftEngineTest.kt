package com.acme.transport.linehaul.backhaul.allocation

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class BackhaulDraftEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBackhaulDraftEngine(newRegistry())
        val item = newBackhaulDraft("code-931", "label-913")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
