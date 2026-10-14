package com.acme.transport.freightpay.accessorial.reservation

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LenientAccessorialAllocationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientAccessorialAllocationEngine(newRegistry())
        val item = newLenientAccessorialAllocation("code-572", "name-282")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
