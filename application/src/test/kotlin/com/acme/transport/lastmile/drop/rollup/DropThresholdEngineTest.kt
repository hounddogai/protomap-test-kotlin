package com.acme.transport.lastmile.drop.rollup

import kotlin.test.Test
import kotlin.test.assertTrue

class DropThresholdEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDropThresholdEngine()
        val item = newDropThreshold("code-653", "title-233")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
