package com.acme.transport.crewing.availability.audit

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyAvailabilityGroupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyAvailabilityGroupEngine(newRegistry())
        val item = newLegacyAvailabilityGroup("code-308", "label-334")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
