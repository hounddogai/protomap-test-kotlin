package com.acme.transport.linehaul.tender.preference

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyTenderPreferenceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyTenderPreferenceEngine(newRegistry())
        val item = newLegacyTenderPreference("code-561", "label-819")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
