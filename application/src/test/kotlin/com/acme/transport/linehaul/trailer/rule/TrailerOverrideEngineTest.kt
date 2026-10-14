package com.acme.transport.linehaul.trailer.rule

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TrailerOverrideEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTrailerOverrideEngine(newRegistry())
        val item = newTrailerOverride("code-205", "title-277")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
