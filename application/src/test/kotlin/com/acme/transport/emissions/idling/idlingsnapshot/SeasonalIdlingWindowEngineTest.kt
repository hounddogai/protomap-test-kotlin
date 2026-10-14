package com.acme.transport.emissions.idling.idlingsnapshot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalIdlingWindowEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalIdlingWindowEngine(newRegistry())
        val item = newSeasonalIdlingWindow("code-420", "label-748")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
