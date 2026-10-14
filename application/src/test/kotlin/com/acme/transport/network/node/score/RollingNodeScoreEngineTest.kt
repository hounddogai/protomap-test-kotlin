package com.acme.transport.network.node.score

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RollingNodeScoreEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingNodeScoreEngine(newRegistry())
        val item = newRollingNodeScore("code-167", "title-796")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
