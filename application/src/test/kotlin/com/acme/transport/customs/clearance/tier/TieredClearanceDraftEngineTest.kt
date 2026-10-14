package com.acme.transport.customs.clearance.tier

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TieredClearanceDraftEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredClearanceDraftEngine(newRegistry())
        val item = newTieredClearanceDraft("code-741", "title-193")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
