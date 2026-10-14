package com.acme.transport.linehaul.capacity.metric

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CapacityProfileEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCapacityProfileEngine(newRegistry())
        val item = newCapacityProfile("code-758", "name-772")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
