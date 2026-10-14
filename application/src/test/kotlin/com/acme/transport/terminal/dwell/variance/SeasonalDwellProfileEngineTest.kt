package com.acme.transport.terminal.dwell.variance

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalDwellProfileEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalDwellProfileEngine(newRegistry())
        val item = newSeasonalDwellProfile("code-494", "label-224")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
