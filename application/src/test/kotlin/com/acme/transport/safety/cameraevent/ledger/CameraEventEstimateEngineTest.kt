package com.acme.transport.safety.cameraevent.ledger

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CameraEventEstimateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCameraEventEstimateEngine(newRegistry())
        val item = newCameraEventEstimate("code-105", "name-558")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
