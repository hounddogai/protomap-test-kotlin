package com.acme.transport.linehaul.capacity.preference

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackCapacityPreferenceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackCapacityPreferenceEngine(newRegistry())
        val item = newFallbackCapacityPreference("code-466", "title-820")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
