package com.acme.transport.linehaul.trailer.request

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TrailerExceptionEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTrailerExceptionEngine(newRegistry())
        val item = newTrailerException("code-818", "label-817")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
