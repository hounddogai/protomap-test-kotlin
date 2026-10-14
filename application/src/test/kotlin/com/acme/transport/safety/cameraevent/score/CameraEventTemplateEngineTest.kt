package com.acme.transport.safety.cameraevent.score

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CameraEventTemplateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCameraEventTemplateEngine(newRegistry())
        val item = newCameraEventTemplate("code-939", "label-935")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
