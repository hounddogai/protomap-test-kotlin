package com.acme.transport.network.cutoff.cutoffentry

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryCutoffEntryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryCutoffEntryEngine(newRegistry())
        val item = newPrimaryCutoffEntry("code-367", "title-623")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
