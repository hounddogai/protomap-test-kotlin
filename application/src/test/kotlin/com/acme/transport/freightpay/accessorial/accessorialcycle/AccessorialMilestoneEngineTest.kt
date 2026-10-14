package com.acme.transport.freightpay.accessorial.accessorialcycle

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AccessorialMilestoneEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAccessorialMilestoneEngine(newRegistry())
        val item = newAccessorialMilestone("code-459", "name-676")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
