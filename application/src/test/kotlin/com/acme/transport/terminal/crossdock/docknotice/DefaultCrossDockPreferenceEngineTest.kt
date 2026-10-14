package com.acme.transport.terminal.crossdock.docknotice

import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultCrossDockPreferenceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultCrossDockPreferenceEngine()
        val item = newDefaultCrossDockPreference("code-15", "name-532")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
