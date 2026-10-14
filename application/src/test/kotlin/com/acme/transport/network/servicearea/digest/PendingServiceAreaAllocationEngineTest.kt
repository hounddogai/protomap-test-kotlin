package com.acme.transport.network.servicearea.digest

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PendingServiceAreaAllocationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingServiceAreaAllocationEngine(newRegistry())
        val item = newPendingServiceAreaAllocation("code-923", "name-161")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
