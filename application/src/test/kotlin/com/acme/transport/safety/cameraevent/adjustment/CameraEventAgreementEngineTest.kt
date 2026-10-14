package com.acme.transport.safety.cameraevent.adjustment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CameraEventAgreementEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCameraEventAgreementEngine(newRegistry())
        val item = newCameraEventAgreement("code-335", "label-505")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
