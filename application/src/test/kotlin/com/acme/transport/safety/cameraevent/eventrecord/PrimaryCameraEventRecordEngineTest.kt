package com.acme.transport.safety.cameraevent.eventrecord

import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryCameraEventRecordEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryCameraEventRecordEngine()
        val item = newPrimaryCameraEventRecord("code-296", "title-843")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
