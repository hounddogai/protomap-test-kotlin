package com.acme.transport.safety.scorecard.scorecardbundle

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryScorecardBundleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryScorecardBundleEngine(newRegistry())
        val item = newPrimaryScorecardBundle("code-917", "title-921")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
