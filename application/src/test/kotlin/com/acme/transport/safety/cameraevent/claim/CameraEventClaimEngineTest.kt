package com.acme.transport.safety.cameraevent.claim

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CameraEventClaimEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCameraEventClaimEngine(newRegistry())
        val item = newCameraEventClaim("code-266", "label-952")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
