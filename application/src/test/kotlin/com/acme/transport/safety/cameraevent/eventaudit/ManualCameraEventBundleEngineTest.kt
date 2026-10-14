package com.acme.transport.safety.cameraevent.eventaudit

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ManualCameraEventBundleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualCameraEventBundleEngine(newRegistry())
        val item = newManualCameraEventBundle("code-825", "name-503")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
