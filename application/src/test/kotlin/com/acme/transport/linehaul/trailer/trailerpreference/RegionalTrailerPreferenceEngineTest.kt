package com.acme.transport.linehaul.trailer.trailerpreference

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalTrailerPreferenceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalTrailerPreferenceEngine(newRegistry())
        val item = newRegionalTrailerPreference("code-825", "title-157")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
