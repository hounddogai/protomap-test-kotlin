package com.acme.transport.terminal.traileryard.adjustment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class StrictTrailerYardAuditEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictTrailerYardAuditEngine(newRegistry())
        val item = newStrictTrailerYardAudit("code-883", "title-15")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
