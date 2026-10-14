package com.acme.transport.crewing.restbreak.breakvariance

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TieredRestBreakManifestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredRestBreakManifestEngine(newRegistry())
        val item = newTieredRestBreakManifest("code-43", "name-977")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
