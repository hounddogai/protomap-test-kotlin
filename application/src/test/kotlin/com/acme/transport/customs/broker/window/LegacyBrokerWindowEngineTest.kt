package com.acme.transport.customs.broker.window

import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyBrokerWindowEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyBrokerWindowEngine()
        val item = newLegacyBrokerWindow("code-814", "name-636")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
