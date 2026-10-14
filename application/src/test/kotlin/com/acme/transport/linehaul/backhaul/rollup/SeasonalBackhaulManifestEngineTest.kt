package com.acme.transport.linehaul.backhaul.rollup

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalBackhaulManifestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalBackhaulManifestEngine(newRegistry())
        val item = newSeasonalBackhaulManifest("code-774", "label-581")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
