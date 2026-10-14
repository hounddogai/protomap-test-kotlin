package com.acme.transport.safety.nearmiss.variance

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TieredNearMissAdjustmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredNearMissAdjustmentEngine(newRegistry())
        val item = newTieredNearMissAdjustment("code-993", "name-516")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
