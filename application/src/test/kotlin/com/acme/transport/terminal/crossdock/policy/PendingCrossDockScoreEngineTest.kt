package com.acme.transport.terminal.crossdock.policy

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PendingCrossDockScoreEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingCrossDockScoreEngine(newRegistry())
        val item = newPendingCrossDockScore("code-42", "title-107")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
