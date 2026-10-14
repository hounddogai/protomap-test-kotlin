package com.acme.transport.crewing.restbreak.breakmapping

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PendingRestBreakMappingEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingRestBreakMappingEngine(newRegistry())
        val item = newPendingRestBreakMapping("code-646", "title-838")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
