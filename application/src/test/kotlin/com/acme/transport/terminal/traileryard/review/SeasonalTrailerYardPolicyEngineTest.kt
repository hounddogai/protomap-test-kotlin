package com.acme.transport.terminal.traileryard.review

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalTrailerYardPolicyEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalTrailerYardPolicyEngine(newRegistry())
        val item = newSeasonalTrailerYardPolicy("code-986", "label-919")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
