package com.acme.transport.safety.coaching.proposal

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedCoachingBundleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedCoachingBundleEngine(newRegistry())
        val item = newWeightedCoachingBundle("code-23", "title-918")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
