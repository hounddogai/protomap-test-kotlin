package com.acme.transport.crewing.availability.availabilityledger

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CappedAvailabilityPreferenceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedAvailabilityPreferenceEngine(newRegistry())
        val item = newCappedAvailabilityPreference("code-71", "title-266")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
