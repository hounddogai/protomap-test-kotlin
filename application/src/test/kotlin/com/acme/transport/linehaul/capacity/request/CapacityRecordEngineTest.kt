package com.acme.transport.linehaul.capacity.request

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CapacityRecordEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCapacityRecordEngine(newRegistry())
        val item = newCapacityRecord("code-900", "name-683")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
