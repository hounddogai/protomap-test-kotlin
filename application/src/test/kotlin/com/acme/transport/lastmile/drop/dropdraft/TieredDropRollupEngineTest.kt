package com.acme.transport.lastmile.drop.dropdraft

import kotlin.test.Test
import kotlin.test.assertTrue

class TieredDropRollupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredDropRollupEngine()
        val item = newTieredDropRollup("code-991", "label-57")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
