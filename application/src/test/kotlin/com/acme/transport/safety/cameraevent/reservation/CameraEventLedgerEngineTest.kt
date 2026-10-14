package com.acme.transport.safety.cameraevent.reservation

import kotlin.test.Test
import kotlin.test.assertTrue

class CameraEventLedgerEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCameraEventLedgerEngine()
        val item = newCameraEventLedger("code-788", "title-813")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
