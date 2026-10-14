package com.acme.transport.safety.nearmiss.preference

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class NearMissPreferenceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultNearMissPreferenceEngine(newRegistry())
        val item = newNearMissPreference("code-185", "label-881")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
