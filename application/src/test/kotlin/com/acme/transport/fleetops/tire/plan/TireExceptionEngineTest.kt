package com.acme.transport.fleetops.tire.plan

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TireExceptionEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTireExceptionEngine(newRegistry())
        val item = newTireException("code-548", "label-668")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
