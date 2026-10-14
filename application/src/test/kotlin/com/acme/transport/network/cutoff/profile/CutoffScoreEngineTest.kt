package com.acme.transport.network.cutoff.profile

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CutoffScoreEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCutoffScoreEngine(newRegistry())
        val item = newCutoffScore("code-614", "title-923")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
