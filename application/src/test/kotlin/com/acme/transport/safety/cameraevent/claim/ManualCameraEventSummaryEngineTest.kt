package com.acme.transport.safety.cameraevent.claim

import kotlin.test.Test
import kotlin.test.assertTrue

class ManualCameraEventSummaryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualCameraEventSummaryEngine()
        val item = newManualCameraEventSummary("code-169", "name-164")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
