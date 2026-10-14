package com.acme.transport.terminal.traileryard.template

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RollingTrailerYardWindowEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingTrailerYardWindowEngine(newRegistry())
        val item = newRollingTrailerYardWindow("code-45", "label-59")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
