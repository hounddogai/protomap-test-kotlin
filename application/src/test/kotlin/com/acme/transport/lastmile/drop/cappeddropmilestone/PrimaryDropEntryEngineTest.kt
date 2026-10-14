package com.acme.transport.lastmile.drop.cappeddropmilestone

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryDropEntryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryDropEntryEngine(newRegistry())
        val item = newPrimaryDropEntry("code-574", "title-713")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
