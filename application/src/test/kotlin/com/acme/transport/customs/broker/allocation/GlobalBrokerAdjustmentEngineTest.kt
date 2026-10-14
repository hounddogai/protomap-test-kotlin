package com.acme.transport.customs.broker.allocation

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalBrokerAdjustmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalBrokerAdjustmentEngine(newRegistry())
        val item = newGlobalBrokerAdjustment("code-406", "label-196")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
