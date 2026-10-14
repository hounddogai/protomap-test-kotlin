package com.acme.transport.terminal.crossdock.exception

import kotlin.test.Test
import kotlin.test.assertTrue

class CrossDockPreferenceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCrossDockPreferenceEngine()
        val item = newCrossDockPreference("code-350", "title-600")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
