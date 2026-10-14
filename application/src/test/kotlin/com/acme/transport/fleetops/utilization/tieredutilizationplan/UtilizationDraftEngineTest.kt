package com.acme.transport.fleetops.utilization.tieredutilizationplan

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class UtilizationDraftEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultUtilizationDraftEngine(newRegistry())
        val item = newUtilizationDraft("code-818", "label-135")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
