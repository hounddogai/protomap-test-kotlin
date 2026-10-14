package com.acme.transport.linehaul.backhaul.backhaulaudit

import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyBackhaulPreferenceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyBackhaulPreferenceEngine()
        val item = newLegacyBackhaulPreference("code-770", "label-718")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
