package com.acme.transport.linehaul.tender.exception

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedTenderExceptionEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedTenderExceptionEngine(newRegistry())
        val item = newWeightedTenderException("code-566", "name-121")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
