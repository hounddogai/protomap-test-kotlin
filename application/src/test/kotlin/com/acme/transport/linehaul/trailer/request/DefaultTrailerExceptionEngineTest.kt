package com.acme.transport.linehaul.trailer.request

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultTrailerExceptionEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultTrailerExceptionEngine(newRegistry())
        val item = newDefaultTrailerException("code-318", "label-485")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
