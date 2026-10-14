package com.acme.transport.lastmile.drop.exception

import kotlin.test.Test
import kotlin.test.assertTrue

class DropExceptionEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDropExceptionEngine()
        val item = newDropException("code-689", "name-947")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
