package com.acme.transport.network.servicearea.exception

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ServiceAreaExceptionEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultServiceAreaExceptionEngine(newRegistry())
        val item = newServiceAreaException("code-885", "title-49")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
