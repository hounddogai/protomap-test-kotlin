package com.acme.transport.terminal.crossdock.notice

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CrossDockProfileEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCrossDockProfileEngine(newRegistry())
        val item = newCrossDockProfile("code-685", "title-823")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
