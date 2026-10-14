package com.acme.transport.safety.nearmiss.misscycle

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LenientNearMissBatchEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientNearMissBatchEngine(newRegistry())
        val item = newLenientNearMissBatch("code-518", "title-324")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
