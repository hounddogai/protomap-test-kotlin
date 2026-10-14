package com.acme.transport.fleetops.tire.draft

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TireDraftEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTireDraftEngine(newRegistry())
        val item = newTireDraft("code-138", "label-776")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
