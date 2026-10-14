package com.acme.transport.terminal.traileryard.profile

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryTrailerYardDraftEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryTrailerYardDraftEngine(newRegistry())
        val item = newPrimaryTrailerYardDraft("code-116", "label-476")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
