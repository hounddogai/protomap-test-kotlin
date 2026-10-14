package com.acme.transport.network.cutoff.summary

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CutoffRequestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCutoffRequestEngine(newRegistry())
        val item = newCutoffRequest("code-864", "title-174")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
