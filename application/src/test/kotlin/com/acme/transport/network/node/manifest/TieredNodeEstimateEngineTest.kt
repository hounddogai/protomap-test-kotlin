package com.acme.transport.network.node.manifest

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TieredNodeEstimateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredNodeEstimateEngine(newRegistry())
        val item = newTieredNodeEstimate("code-529", "title-170")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
