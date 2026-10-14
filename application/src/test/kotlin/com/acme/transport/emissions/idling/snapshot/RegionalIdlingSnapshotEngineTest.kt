package com.acme.transport.emissions.idling.snapshot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalIdlingSnapshotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalIdlingSnapshotEngine(newRegistry())
        val item = newRegionalIdlingSnapshot("code-760", "label-362")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
