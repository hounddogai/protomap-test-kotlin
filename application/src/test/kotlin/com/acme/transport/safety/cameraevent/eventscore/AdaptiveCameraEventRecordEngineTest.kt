package com.acme.transport.safety.cameraevent.eventscore

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveCameraEventRecordEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveCameraEventRecordEngine(newRegistry())
        val item = newAdaptiveCameraEventRecord("code-481", "title-945")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
