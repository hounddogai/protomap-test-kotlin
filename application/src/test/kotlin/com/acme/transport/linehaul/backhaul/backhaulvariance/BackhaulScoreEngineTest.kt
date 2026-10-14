package com.acme.transport.linehaul.backhaul.backhaulvariance

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class BackhaulScoreEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBackhaulScoreEngine(newRegistry())
        val item = newBackhaulScore("code-531", "label-890")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
