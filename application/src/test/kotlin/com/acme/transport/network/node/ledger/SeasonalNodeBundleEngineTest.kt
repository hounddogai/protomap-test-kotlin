package com.acme.transport.network.node.ledger

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalNodeBundleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalNodeBundleEngine(newRegistry())
        val item = newSeasonalNodeBundle("code-392", "title-672")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
