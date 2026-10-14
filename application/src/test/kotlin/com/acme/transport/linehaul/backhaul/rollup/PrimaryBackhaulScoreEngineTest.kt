package com.acme.transport.linehaul.backhaul.rollup

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryBackhaulScoreEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryBackhaulScoreEngine(newRegistry())
        val item = newPrimaryBackhaulScore("code-926", "title-841")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
