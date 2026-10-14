package com.acme.transport.safety.cameraevent.rollup

import kotlin.test.Test
import kotlin.test.assertTrue

class CameraEventManifestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCameraEventManifestEngine()
        val item = newCameraEventManifest("code-700", "name-643")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
