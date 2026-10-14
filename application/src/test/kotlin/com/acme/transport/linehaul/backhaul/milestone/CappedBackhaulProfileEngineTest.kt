package com.acme.transport.linehaul.backhaul.milestone

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CappedBackhaulProfileEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedBackhaulProfileEngine(newRegistry())
        val item = newCappedBackhaulProfile("code-502", "name-796")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
