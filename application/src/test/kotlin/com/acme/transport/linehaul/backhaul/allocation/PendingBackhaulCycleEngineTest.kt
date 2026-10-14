package com.acme.transport.linehaul.backhaul.allocation

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PendingBackhaulCycleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingBackhaulCycleEngine(newRegistry())
        val item = newPendingBackhaulCycle("code-539", "name-595")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
