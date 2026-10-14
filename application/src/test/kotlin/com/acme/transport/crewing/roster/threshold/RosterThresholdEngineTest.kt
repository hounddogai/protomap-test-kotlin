package com.acme.transport.crewing.roster.threshold

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RosterThresholdEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRosterThresholdEngine(newRegistry())
        val item = newRosterThreshold("code-635", "title-76")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
