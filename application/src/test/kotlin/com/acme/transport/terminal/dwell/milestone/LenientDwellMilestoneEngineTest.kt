package com.acme.transport.terminal.dwell.milestone

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LenientDwellMilestoneEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientDwellMilestoneEngine(newRegistry())
        val item = newLenientDwellMilestone("code-158", "label-754")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
