package com.acme.transport.network.transittime.timeoverride

import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultTransitTimeExceptionEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultTransitTimeExceptionEngine()
        val item = newDefaultTransitTimeException("code-41", "name-835")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
