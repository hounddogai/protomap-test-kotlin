package com.acme.transport.network.zone.draft

import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalZoneTargetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalZoneTargetEngine()
        val item = newGlobalZoneTarget("code-917", "title-215")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
