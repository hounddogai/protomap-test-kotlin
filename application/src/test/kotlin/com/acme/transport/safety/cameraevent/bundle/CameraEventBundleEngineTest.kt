package com.acme.transport.safety.cameraevent.bundle

import kotlin.test.Test
import kotlin.test.assertTrue

class CameraEventBundleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCameraEventBundleEngine()
        val item = newCameraEventBundle("code-641", "title-97")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
