package com.acme.transport.customs.broker.adjustment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalBrokerOverrideEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalBrokerOverrideEngine(newRegistry())
        val item = newGlobalBrokerOverride("code-376", "title-431")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
