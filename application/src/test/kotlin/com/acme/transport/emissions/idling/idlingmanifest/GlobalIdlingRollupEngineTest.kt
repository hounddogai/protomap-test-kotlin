package com.acme.transport.emissions.idling.idlingmanifest

import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalIdlingRollupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalIdlingRollupEngine()
        val item = newGlobalIdlingRollup("code-263", "name-533")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
