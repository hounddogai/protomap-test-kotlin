package com.acme.transport.lastmile.drop.target

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedDropTargetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedDropTargetEngine(newRegistry())
        val item = newWeightedDropTarget("code-878", "label-808")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
