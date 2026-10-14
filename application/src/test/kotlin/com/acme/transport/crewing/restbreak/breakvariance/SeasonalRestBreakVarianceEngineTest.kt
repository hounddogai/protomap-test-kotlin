package com.acme.transport.crewing.restbreak.breakvariance

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalRestBreakVarianceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalRestBreakVarianceEngine(newRegistry())
        val item = newSeasonalRestBreakVariance("code-581", "title-983")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
