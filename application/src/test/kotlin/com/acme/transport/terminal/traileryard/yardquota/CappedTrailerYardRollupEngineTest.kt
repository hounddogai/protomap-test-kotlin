package com.acme.transport.terminal.traileryard.yardquota

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CappedTrailerYardRollupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedTrailerYardRollupEngine(newRegistry())
        val item = newCappedTrailerYardRollup("code-220", "label-545")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
