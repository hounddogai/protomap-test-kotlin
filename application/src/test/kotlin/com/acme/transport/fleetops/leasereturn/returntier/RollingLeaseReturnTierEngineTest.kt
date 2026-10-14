package com.acme.transport.fleetops.leasereturn.returntier

import kotlin.test.Test
import kotlin.test.assertTrue

class RollingLeaseReturnTierEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingLeaseReturnTierEngine()
        val item = newRollingLeaseReturnTier("code-542", "name-980")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
