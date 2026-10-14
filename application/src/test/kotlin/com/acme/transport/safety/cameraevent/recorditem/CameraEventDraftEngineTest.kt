package com.acme.transport.safety.cameraevent.recorditem

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CameraEventDraftEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCameraEventDraftEngine(newRegistry())
        val item = newCameraEventDraft("code-638", "label-928")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
