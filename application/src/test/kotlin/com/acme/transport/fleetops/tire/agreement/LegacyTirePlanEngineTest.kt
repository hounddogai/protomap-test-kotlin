package com.acme.transport.fleetops.tire.agreement

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyTirePlanEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyTirePlanEngine(newRegistry())
        val item = newLegacyTirePlan("code-590", "title-325")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
