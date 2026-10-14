package com.acme.transport.linehaul.backhaul.threshold

import kotlin.test.Test
import kotlin.test.assertTrue

class BackhaulSnapshotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBackhaulSnapshotEngine()
        val item = newBackhaulSnapshot("code-807", "name-818")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
