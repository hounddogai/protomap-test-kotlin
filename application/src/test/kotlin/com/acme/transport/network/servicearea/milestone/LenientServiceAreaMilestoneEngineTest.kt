package com.acme.transport.network.servicearea.milestone

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LenientServiceAreaMilestoneEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientServiceAreaMilestoneEngine(newRegistry())
        val item = newLenientServiceAreaMilestone("code-377", "label-656")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
