package com.acme.transport.lastmile.redelivery.quota

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RedeliverySnapshotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRedeliverySnapshotEngine(newRegistry())
        val item = newRedeliverySnapshot("code-396", "label-431")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
