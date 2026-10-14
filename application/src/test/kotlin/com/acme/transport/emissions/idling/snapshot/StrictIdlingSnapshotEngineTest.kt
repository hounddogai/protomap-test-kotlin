package com.acme.transport.emissions.idling.snapshot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class StrictIdlingSnapshotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictIdlingSnapshotEngine(newRegistry())
        val item = newStrictIdlingSnapshot("code-125", "label-112")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
