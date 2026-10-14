package com.acme.transport.lastmile.drop.dropexception

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyDropExceptionEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyDropExceptionEngine(newRegistry())
        val item = newLegacyDropException("code-666", "label-989")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
