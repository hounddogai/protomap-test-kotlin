package com.acme.transport.network.servicearea.areaassignment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ServiceAreaSnapshotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultServiceAreaSnapshotEngine(newRegistry())
        val item = newServiceAreaSnapshot("code-440", "title-54")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
