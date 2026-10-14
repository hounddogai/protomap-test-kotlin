package com.acme.transport.fleetops.utilization.exception

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalUtilizationExceptionEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalUtilizationExceptionEngine(newRegistry())
        val item = newGlobalUtilizationException("code-487", "name-386")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
