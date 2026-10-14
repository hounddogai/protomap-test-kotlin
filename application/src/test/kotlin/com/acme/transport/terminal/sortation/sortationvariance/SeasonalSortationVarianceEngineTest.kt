package com.acme.transport.terminal.sortation.sortationvariance

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalSortationVarianceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalSortationVarianceEngine(newRegistry())
        val item = newSeasonalSortationVariance("code-737", "label-765")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
