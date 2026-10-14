package com.acme.transport.safety.nearmiss.segment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalNearMissRequestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalNearMissRequestEngine(newRegistry())
        val item = newGlobalNearMissRequest("code-892", "title-560")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
