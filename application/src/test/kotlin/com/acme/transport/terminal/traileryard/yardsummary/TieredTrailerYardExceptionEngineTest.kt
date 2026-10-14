package com.acme.transport.terminal.traileryard.yardsummary

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TieredTrailerYardExceptionEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredTrailerYardExceptionEngine(newRegistry())
        val item = newTieredTrailerYardException("code-715", "label-390")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
