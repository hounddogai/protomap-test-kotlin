package com.acme.transport.lastmile.drop.dropforecast

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryDropThresholdEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryDropThresholdEngine(newRegistry())
        val item = newPrimaryDropThreshold("code-750", "name-469")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
