package com.acme.transport.linehaul.capacity.allocation

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CapacityExceptionEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCapacityExceptionEngine(newRegistry())
        val item = newCapacityException("code-840", "name-898")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
