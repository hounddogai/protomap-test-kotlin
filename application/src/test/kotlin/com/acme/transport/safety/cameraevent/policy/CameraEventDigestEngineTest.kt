package com.acme.transport.safety.cameraevent.policy

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CameraEventDigestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCameraEventDigestEngine(newRegistry())
        val item = newCameraEventDigest("code-609", "label-88")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
