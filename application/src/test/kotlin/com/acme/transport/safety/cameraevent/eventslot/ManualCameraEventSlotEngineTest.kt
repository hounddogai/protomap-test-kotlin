package com.acme.transport.safety.cameraevent.eventslot

import kotlin.test.Test
import kotlin.test.assertTrue

class ManualCameraEventSlotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualCameraEventSlotEngine()
        val item = newManualCameraEventSlot("code-414", "label-252")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
