package com.acme.transport.safety.cameraevent.recorditem

import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackCameraEventRecordEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackCameraEventRecordEngine()
        val item = newFallbackCameraEventRecord("code-424", "title-922")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
