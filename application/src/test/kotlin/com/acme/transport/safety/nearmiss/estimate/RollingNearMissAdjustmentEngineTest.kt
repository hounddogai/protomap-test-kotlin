package com.acme.transport.safety.nearmiss.estimate

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RollingNearMissAdjustmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingNearMissAdjustmentEngine(newRegistry())
        val item = newRollingNearMissAdjustment("code-576", "label-762")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
