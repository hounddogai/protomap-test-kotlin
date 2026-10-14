package com.acme.transport.terminal.sortation.request

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class StrictSortationManifestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictSortationManifestEngine(newRegistry())
        val item = newStrictSortationManifest("code-552", "label-376")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
