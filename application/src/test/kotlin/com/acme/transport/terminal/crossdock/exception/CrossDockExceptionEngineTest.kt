package com.acme.transport.terminal.crossdock.exception

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CrossDockExceptionEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCrossDockExceptionEngine(newRegistry())
        val item = newCrossDockException("code-334", "name-582")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
