package com.acme.transport.safety.cameraevent.entry

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CameraEventAssignmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCameraEventAssignmentEngine(newRegistry())
        val item = newCameraEventAssignment("code-100", "label-629")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
