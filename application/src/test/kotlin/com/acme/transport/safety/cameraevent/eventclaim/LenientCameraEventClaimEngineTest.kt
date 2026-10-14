package com.acme.transport.safety.cameraevent.eventclaim

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LenientCameraEventClaimEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientCameraEventClaimEngine(newRegistry())
        val item = newLenientCameraEventClaim("code-670", "title-558")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
