package com.acme.transport.lastmile.drop.dropslot

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DropEstimateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDropEstimateEngine(newRegistry())
        val item = newDropEstimate("code-998", "label-222")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
