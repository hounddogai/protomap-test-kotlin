package com.acme.transport.customs.broker.group

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultBrokerOverrideEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultBrokerOverrideEngine(newRegistry())
        val item = newDefaultBrokerOverride("code-778", "label-372")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
