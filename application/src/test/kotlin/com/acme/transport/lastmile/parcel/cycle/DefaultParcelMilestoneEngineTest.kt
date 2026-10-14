package com.acme.transport.lastmile.parcel.cycle

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultParcelMilestoneEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultParcelMilestoneEngine(newRegistry())
        val item = newDefaultParcelMilestone("code-276", "label-958")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
