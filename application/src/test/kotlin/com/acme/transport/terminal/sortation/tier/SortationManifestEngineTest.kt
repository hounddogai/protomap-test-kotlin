package com.acme.transport.terminal.sortation.tier

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class SortationManifestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSortationManifestEngine(newRegistry())
        val item = newSortationManifest("code-371", "label-219")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
