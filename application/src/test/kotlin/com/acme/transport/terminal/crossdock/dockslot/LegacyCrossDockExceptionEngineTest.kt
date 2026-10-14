package com.acme.transport.terminal.crossdock.dockslot

import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyCrossDockExceptionEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyCrossDockExceptionEngine()
        val item = newLegacyCrossDockException("code-268", "title-239")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
