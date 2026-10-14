package com.acme.transport.safety.cameraevent.globaleventaudit

import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveCameraEventAllocationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveCameraEventAllocationEngine()
        val item = newAdaptiveCameraEventAllocation("code-542", "label-919")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
