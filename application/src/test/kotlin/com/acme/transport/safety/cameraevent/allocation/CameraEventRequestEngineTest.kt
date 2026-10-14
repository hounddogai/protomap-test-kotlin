package com.acme.transport.safety.cameraevent.allocation

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CameraEventRequestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCameraEventRequestEngine(newRegistry())
        val item = newCameraEventRequest("code-760", "label-540")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
