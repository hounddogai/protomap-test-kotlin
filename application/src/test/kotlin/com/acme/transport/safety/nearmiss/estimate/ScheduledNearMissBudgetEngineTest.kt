package com.acme.transport.safety.nearmiss.estimate

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledNearMissBudgetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledNearMissBudgetEngine(newRegistry())
        val item = newScheduledNearMissBudget("code-812", "title-633")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
