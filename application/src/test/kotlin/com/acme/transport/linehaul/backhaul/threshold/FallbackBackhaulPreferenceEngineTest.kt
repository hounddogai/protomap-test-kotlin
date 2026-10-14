package com.acme.transport.linehaul.backhaul.threshold

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackBackhaulPreferenceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackBackhaulPreferenceEngine(newRegistry())
        val item = newFallbackBackhaulPreference("code-889", "name-221")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
