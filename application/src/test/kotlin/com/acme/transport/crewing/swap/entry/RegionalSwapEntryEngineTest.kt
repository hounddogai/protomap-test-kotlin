package com.acme.transport.crewing.swap.entry

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalSwapEntryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalSwapEntryEngine(newRegistry())
        val item = newRegionalSwapEntry("code-506", "title-288")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
