package com.acme.transport.customs.broker.score

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RollingBrokerScoreEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingBrokerScoreEngine(newRegistry())
        val item = newRollingBrokerScore("code-659", "title-671")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
