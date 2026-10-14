package com.acme.transport.terminal.crossdock.regionaldockclaim

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalCrossDockPreferenceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalCrossDockPreferenceEngine(newRegistry())
        val item = newGlobalCrossDockPreference("code-512", "label-174")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
