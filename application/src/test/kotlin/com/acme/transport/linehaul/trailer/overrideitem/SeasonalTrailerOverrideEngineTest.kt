package com.acme.transport.linehaul.trailer.overrideitem

import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalTrailerOverrideEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalTrailerOverrideEngine()
        val item = newSeasonalTrailerOverride("code-994", "label-43")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
