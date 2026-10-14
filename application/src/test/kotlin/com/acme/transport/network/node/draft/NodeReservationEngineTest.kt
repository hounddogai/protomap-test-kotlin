package com.acme.transport.network.node.draft

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class NodeReservationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultNodeReservationEngine(newRegistry())
        val item = newNodeReservation("code-741", "title-856")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
