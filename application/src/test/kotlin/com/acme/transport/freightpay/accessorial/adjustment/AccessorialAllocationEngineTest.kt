package com.acme.transport.freightpay.accessorial.adjustment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AccessorialAllocationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAccessorialAllocationEngine(newRegistry())
        val item = newAccessorialAllocation("code-339", "title-735")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
