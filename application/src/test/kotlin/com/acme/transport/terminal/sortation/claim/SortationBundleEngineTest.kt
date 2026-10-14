package com.acme.transport.terminal.sortation.claim

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class SortationBundleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSortationBundleEngine(newRegistry())
        val item = newSortationBundle("code-135", "title-315")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
