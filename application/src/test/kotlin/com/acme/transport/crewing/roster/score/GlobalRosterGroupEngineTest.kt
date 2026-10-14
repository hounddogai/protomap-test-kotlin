package com.acme.transport.crewing.roster.score

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalRosterGroupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalRosterGroupEngine(newRegistry())
        val item = newGlobalRosterGroup("code-285", "title-577")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
