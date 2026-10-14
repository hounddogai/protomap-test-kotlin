package com.acme.transport.network.zone.zonequota

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryZoneAllocationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryZoneAllocationEngine(newRegistry())
        val item = newPrimaryZoneAllocation("code-348", "title-448")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
