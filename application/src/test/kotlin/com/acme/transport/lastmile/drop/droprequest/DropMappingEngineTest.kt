package com.acme.transport.lastmile.drop.droprequest

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DropMappingEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDropMappingEngine(newRegistry())
        val item = newDropMapping("code-472", "label-595")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
