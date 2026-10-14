package com.acme.transport.terminal.traileryard.audit

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultTrailerYardWindowEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultTrailerYardWindowEngine(newRegistry())
        val item = newDefaultTrailerYardWindow("code-688", "label-97")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
