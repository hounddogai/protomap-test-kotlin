package com.acme.transport.safety.cameraevent.batch

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CameraEventTargetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCameraEventTargetEngine(newRegistry())
        val item = newCameraEventTarget("code-857", "label-457")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
