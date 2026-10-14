package com.acme.transport.terminal.dockdoor.manifest

import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveDockDoorBaselineEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveDockDoorBaselineEngine()
        val item = newAdaptiveDockDoorBaseline("code-663", "title-646")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
