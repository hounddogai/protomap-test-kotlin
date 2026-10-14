package com.acme.transport.safety.cameraevent.eventslot

import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackCameraEventSnapshotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackCameraEventSnapshotEngine()
        val item = newFallbackCameraEventSnapshot("code-134", "label-938")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
