package com.acme.transport.network.servicearea.snapshot

import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalServiceAreaSnapshotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalServiceAreaSnapshotEngine()
        val item = newGlobalServiceAreaSnapshot("code-662", "name-852")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
