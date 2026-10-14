package com.acme.transport.network.cutoff.preference

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PendingCutoffScoreEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingCutoffScoreEngine(newRegistry())
        val item = newPendingCutoffScore("code-969", "label-691")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
