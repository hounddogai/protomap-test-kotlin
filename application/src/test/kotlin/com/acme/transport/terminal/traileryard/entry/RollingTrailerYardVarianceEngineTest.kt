package com.acme.transport.terminal.traileryard.entry

import kotlin.test.Test
import kotlin.test.assertTrue

class RollingTrailerYardVarianceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingTrailerYardVarianceEngine()
        val item = newRollingTrailerYardVariance("code-831", "name-415")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
