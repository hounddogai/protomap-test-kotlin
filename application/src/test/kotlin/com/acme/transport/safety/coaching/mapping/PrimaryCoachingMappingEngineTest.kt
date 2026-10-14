package com.acme.transport.safety.coaching.mapping

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryCoachingMappingEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryCoachingMappingEngine(newRegistry())
        val item = newPrimaryCoachingMapping("code-31", "title-516")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
