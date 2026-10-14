package com.acme.transport.customs.clearance.policy

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ClearanceSnapshotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultClearanceSnapshotEngine(newRegistry())
        val item = newClearanceSnapshot("code-432", "title-318")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
