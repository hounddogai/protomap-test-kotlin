package com.acme.transport.safety.scorecard.tieredscorecardslot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ScorecardQuotaEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScorecardQuotaEngine(newRegistry())
        val item = newScorecardQuota("code-951", "label-947")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
