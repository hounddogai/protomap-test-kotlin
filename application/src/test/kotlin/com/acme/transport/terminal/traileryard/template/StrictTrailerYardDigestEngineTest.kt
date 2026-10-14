package com.acme.transport.terminal.traileryard.template

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class StrictTrailerYardDigestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictTrailerYardDigestEngine(newRegistry())
        val item = newStrictTrailerYardDigest("code-633", "name-961")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
