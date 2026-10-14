package com.acme.transport.safety.cameraevent.bundle

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CameraEventBaselineEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCameraEventBaselineEngine(newRegistry())
        val item = newCameraEventBaseline("code-569", "label-241")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
