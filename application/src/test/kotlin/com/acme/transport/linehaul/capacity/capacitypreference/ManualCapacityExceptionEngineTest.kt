package com.acme.transport.linehaul.capacity.capacitypreference

import kotlin.test.Test
import kotlin.test.assertTrue

class ManualCapacityExceptionEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualCapacityExceptionEngine()
        val item = newManualCapacityException("code-670", "label-497")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
