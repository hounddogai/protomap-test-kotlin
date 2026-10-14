package com.acme.transport.customs.broker.adjustment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RollingBrokerAdjustmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingBrokerAdjustmentEngine(newRegistry())
        val item = newRollingBrokerAdjustment("code-16", "name-175")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
