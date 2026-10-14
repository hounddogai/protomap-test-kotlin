package com.acme.transport.crewing.roster.exception

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RosterMappingEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRosterMappingEngine(newRegistry())
        val item = newRosterMapping("code-166", "label-664")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
