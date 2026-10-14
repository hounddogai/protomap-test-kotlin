package com.acme.transport.safety.cameraevent.budget

import kotlin.test.Test
import kotlin.test.assertTrue

class CameraEventMilestoneEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCameraEventMilestoneEngine()
        val item = newCameraEventMilestone("code-986", "title-817")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
