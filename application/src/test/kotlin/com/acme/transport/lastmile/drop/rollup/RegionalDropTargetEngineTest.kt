package com.acme.transport.lastmile.drop.rollup

import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalDropTargetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalDropTargetEngine()
        val item = newRegionalDropTarget("code-825", "title-683")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
