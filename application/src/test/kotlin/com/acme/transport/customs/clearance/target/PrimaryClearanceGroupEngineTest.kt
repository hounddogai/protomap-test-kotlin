package com.acme.transport.customs.clearance.target

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryClearanceGroupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryClearanceGroupEngine(newRegistry())
        val item = newPrimaryClearanceGroup("code-422", "name-751")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
