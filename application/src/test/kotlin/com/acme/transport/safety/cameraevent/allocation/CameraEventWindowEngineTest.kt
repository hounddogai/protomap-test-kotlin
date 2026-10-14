package com.acme.transport.safety.cameraevent.allocation

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CameraEventWindowEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCameraEventWindowEngine(newRegistry())
        val item = newCameraEventWindow("code-647", "label-937")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
