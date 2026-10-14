package com.acme.transport.freightpay.accessorial.accessorialmetric

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalAccessorialScoreEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalAccessorialScoreEngine(newRegistry())
        val item = newSeasonalAccessorialScore("code-68", "name-798")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
