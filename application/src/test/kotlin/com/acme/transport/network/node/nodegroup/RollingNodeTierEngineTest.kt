package com.acme.transport.network.node.nodegroup

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RollingNodeTierEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingNodeTierEngine(newRegistry())
        val item = newRollingNodeTier("code-886", "name-278")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
