package com.acme.transport.safety.cameraevent.tier

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class StrictCameraEventTierEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictCameraEventTierEngine(newRegistry())
        val item = newStrictCameraEventTier("code-511", "name-110")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
