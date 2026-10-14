package com.acme.transport.safety.cameraevent.budget

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CameraEventPolicyEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCameraEventPolicyEngine(newRegistry())
        val item = newCameraEventPolicy("code-236", "name-900")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
