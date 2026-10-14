package com.acme.transport.terminal.traileryard.yardcycle

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyTrailerYardBundleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyTrailerYardBundleEngine(newRegistry())
        val item = newLegacyTrailerYardBundle("code-102", "name-318")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
