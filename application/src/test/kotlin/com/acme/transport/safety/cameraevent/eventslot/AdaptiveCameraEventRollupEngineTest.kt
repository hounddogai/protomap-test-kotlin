package com.acme.transport.safety.cameraevent.eventslot

import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveCameraEventRollupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveCameraEventRollupEngine()
        val item = newAdaptiveCameraEventRollup("code-199", "name-728")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
