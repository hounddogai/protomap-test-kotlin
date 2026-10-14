package com.acme.transport.customs.hold.request

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CappedHoldRequestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedHoldRequestEngine(newRegistry())
        val item = newCappedHoldRequest("code-264", "title-970")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
