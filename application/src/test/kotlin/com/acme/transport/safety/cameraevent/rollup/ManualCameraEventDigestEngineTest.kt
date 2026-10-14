package com.acme.transport.safety.cameraevent.rollup

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ManualCameraEventDigestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualCameraEventDigestEngine(newRegistry())
        val item = newManualCameraEventDigest("code-571", "label-464")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
