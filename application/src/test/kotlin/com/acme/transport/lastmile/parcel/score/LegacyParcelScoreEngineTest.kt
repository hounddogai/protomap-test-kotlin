package com.acme.transport.lastmile.parcel.score

import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyParcelScoreEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyParcelScoreEngine()
        val item = newLegacyParcelScore("code-815", "title-497")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
