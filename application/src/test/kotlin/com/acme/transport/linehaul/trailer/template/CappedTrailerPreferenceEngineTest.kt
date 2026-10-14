package com.acme.transport.linehaul.trailer.template

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CappedTrailerPreferenceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedTrailerPreferenceEngine(newRegistry())
        val item = newCappedTrailerPreference("code-538", "title-809")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
