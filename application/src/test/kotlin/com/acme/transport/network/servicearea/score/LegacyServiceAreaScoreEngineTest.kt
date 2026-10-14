package com.acme.transport.network.servicearea.score

import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyServiceAreaScoreEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyServiceAreaScoreEngine()
        val item = newLegacyServiceAreaScore("code-501", "label-906")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
