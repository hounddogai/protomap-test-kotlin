package com.acme.transport.linehaul.trailer.request

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TrailerRequestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTrailerRequestEngine(newRegistry())
        val item = newTrailerRequest("code-13", "title-960")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
