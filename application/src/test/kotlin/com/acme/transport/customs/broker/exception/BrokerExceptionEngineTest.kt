package com.acme.transport.customs.broker.exception

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class BrokerExceptionEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBrokerExceptionEngine(newRegistry())
        val item = newBrokerException("code-366", "name-278")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
