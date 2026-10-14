package com.acme.transport.freightpay.accessorial.profile

import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultAccessorialAllocationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultAccessorialAllocationEngine()
        val item = newDefaultAccessorialAllocation("code-355", "label-174")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
