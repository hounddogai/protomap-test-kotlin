package com.acme.transport.linehaul.backhaul.primarybackhaulvariance

import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalBackhaulPreferenceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalBackhaulPreferenceEngine()
        val item = newGlobalBackhaulPreference("code-704", "label-235")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
