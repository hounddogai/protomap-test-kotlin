package com.acme.transport.linehaul.trailer.exception

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class StrictTrailerManifestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictTrailerManifestEngine(newRegistry())
        val item = newStrictTrailerManifest("code-524", "title-351")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
