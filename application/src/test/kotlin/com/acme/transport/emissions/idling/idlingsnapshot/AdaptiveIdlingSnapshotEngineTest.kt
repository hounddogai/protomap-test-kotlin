package com.acme.transport.emissions.idling.idlingsnapshot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveIdlingSnapshotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveIdlingSnapshotEngine(newRegistry())
        val item = newAdaptiveIdlingSnapshot("code-95", "title-970")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
