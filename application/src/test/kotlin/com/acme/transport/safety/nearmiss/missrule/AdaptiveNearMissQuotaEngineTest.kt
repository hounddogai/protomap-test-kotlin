package com.acme.transport.safety.nearmiss.missrule

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveNearMissQuotaEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveNearMissQuotaEngine(newRegistry())
        val item = newAdaptiveNearMissQuota("code-294", "label-612")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
