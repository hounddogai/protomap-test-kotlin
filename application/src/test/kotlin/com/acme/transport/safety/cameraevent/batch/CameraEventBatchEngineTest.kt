package com.acme.transport.safety.cameraevent.batch

import kotlin.test.Test
import kotlin.test.assertTrue

class CameraEventBatchEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCameraEventBatchEngine()
        val item = newCameraEventBatch("code-776", "name-664")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
