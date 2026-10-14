package com.acme.transport.network.zone.pendingzonemapping

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PendingZoneMappingEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingZoneMappingEngine(newRegistry())
        val item = newPendingZoneMapping("code-271", "label-999")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
