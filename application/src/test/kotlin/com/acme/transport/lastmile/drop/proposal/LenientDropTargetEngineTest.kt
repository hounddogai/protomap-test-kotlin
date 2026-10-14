package com.acme.transport.lastmile.drop.proposal

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LenientDropTargetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientDropTargetEngine(newRegistry())
        val item = newLenientDropTarget("code-923", "title-244")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
