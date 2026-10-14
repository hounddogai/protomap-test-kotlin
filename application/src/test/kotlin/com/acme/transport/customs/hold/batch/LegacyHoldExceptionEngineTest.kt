package com.acme.transport.customs.hold.batch

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyHoldExceptionEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyHoldExceptionEngine(newRegistry())
        val item = newLegacyHoldException("code-429", "name-14")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
