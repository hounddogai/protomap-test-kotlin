package com.acme.transport.network.cutoff.preference

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CutoffPreferenceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCutoffPreferenceEngine(newRegistry())
        val item = newCutoffPreference("code-442", "title-77")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
