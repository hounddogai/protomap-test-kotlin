package com.acme.transport.safety.cameraevent.eventaudit

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RollingCameraEventAuditEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingCameraEventAuditEngine(newRegistry())
        val item = newRollingCameraEventAudit("code-800", "label-686")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
