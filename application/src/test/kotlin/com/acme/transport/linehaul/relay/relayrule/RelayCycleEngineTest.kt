package com.acme.transport.linehaul.relay.relayrule

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RelayCycleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRelayCycleEngine(newRegistry())
        val item = newRelayCycle("code-597", "label-623")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
