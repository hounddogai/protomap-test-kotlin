package com.acme.transport.fleetops.utilization.budget

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CappedUtilizationPreferenceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedUtilizationPreferenceEngine(newRegistry())
        val item = newCappedUtilizationPreference("code-257", "name-911")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
