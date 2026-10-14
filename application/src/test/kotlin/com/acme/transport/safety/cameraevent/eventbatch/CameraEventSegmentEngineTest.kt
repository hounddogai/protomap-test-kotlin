package com.acme.transport.safety.cameraevent.eventbatch

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CameraEventSegmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCameraEventSegmentEngine(newRegistry())
        val item = newCameraEventSegment("code-797", "title-760")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
