package com.acme.transport.network.cutoff.entry

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CutoffEntryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCutoffEntryEngine(newRegistry())
        val item = newCutoffEntry("code-58", "name-519")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
