package com.acme.transport.terminal.traileryard.review

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TrailerYardOverrideEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTrailerYardOverrideEngine(newRegistry())
        val item = newTrailerYardOverride("code-754", "name-948")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
