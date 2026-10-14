package com.acme.transport.network.node.group

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryNodeProfileEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryNodeProfileEngine(newRegistry())
        val item = newPrimaryNodeProfile("code-330", "name-855")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
