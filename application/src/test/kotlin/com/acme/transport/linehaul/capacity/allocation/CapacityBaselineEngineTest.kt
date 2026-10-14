package com.acme.transport.linehaul.capacity.allocation

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CapacityBaselineEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCapacityBaselineEngine(newRegistry())
        val item = newCapacityBaseline("code-986", "title-343")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
