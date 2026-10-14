package com.acme.transport.terminal.crossdock.dockestimate

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RollingCrossDockScoreEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingCrossDockScoreEngine(newRegistry())
        val item = newRollingCrossDockScore("code-127", "title-275")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
