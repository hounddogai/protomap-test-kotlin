package com.acme.transport.network.node.schedule

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class NodeScheduleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultNodeScheduleEngine(newRegistry())
        val item = newNodeSchedule("code-489", "label-945")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
