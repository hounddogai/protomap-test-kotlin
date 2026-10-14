package com.acme.transport.safety.cameraevent.group

import kotlin.test.Test
import kotlin.test.assertTrue

class CameraEventOverrideEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCameraEventOverrideEngine()
        val item = newCameraEventOverride("code-554", "name-758")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
