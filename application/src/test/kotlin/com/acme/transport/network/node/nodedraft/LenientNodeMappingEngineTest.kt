package com.acme.transport.network.node.nodedraft

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LenientNodeMappingEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientNodeMappingEngine(newRegistry())
        val item = newLenientNodeMapping("code-111", "title-542")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
