package com.acme.transport.safety.cameraevent.defaulteventscore

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveCameraEventGroupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveCameraEventGroupEngine(newRegistry())
        val item = newAdaptiveCameraEventGroup("code-849", "title-775")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
