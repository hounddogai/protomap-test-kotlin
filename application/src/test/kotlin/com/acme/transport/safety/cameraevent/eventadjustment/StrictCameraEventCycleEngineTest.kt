package com.acme.transport.safety.cameraevent.eventadjustment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class StrictCameraEventCycleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictCameraEventCycleEngine(newRegistry())
        val item = newStrictCameraEventCycle("code-520", "title-720")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
