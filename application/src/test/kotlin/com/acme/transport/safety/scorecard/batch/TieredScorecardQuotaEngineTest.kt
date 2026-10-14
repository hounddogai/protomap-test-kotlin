package com.acme.transport.safety.scorecard.batch

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TieredScorecardQuotaEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredScorecardQuotaEngine(newRegistry())
        val item = newTieredScorecardQuota("code-635", "name-379")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
