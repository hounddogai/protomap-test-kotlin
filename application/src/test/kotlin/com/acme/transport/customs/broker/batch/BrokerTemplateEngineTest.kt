package com.acme.transport.customs.broker.batch

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class BrokerTemplateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBrokerTemplateEngine(newRegistry())
        val item = newBrokerTemplate("code-847", "label-819")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
