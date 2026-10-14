package com.acme.transport.safety.cameraevent.summary

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CameraEventProfileEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCameraEventProfileEngine(newRegistry())
        val item = newCameraEventProfile("code-551", "title-336")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
