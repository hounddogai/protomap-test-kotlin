package com.acme.transport.safety.cameraevent.group

import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultCameraEventGroupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultCameraEventGroupEngine()
        val item = newDefaultCameraEventGroup("code-193", "label-833")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
