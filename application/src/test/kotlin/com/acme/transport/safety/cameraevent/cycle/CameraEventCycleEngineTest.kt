package com.acme.transport.safety.cameraevent.cycle

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CameraEventCycleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCameraEventCycleEngine(newRegistry())
        val item = newCameraEventCycle("code-691", "label-541")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
