package com.acme.transport.lastmile.drop.profile

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DropAdjustmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDropAdjustmentEngine(newRegistry())
        val item = newDropAdjustment("code-495", "title-437")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
