package com.acme.transport.customs.clearance.target

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ClearanceExceptionEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultClearanceExceptionEngine(newRegistry())
        val item = newClearanceException("code-260", "name-910")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
