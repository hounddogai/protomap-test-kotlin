package com.acme.transport.terminal.crossdock.summary

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CrossDockMappingEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCrossDockMappingEngine(newRegistry())
        val item = newCrossDockMapping("code-682", "title-690")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
