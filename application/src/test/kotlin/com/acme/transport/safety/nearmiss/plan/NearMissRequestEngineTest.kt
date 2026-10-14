package com.acme.transport.safety.nearmiss.plan

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class NearMissRequestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultNearMissRequestEngine(newRegistry())
        val item = newNearMissRequest("code-373", "label-291")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
