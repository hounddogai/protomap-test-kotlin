package com.acme.transport.safety.cameraevent.policy

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ManualCameraEventPolicyEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualCameraEventPolicyEngine(newRegistry())
        val item = newManualCameraEventPolicy("code-809", "name-433")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
