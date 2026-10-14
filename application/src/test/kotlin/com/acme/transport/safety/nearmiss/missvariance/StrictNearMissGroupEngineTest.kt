package com.acme.transport.safety.nearmiss.missvariance

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class StrictNearMissGroupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictNearMissGroupEngine(newRegistry())
        val item = newStrictNearMissGroup("code-386", "name-417")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
