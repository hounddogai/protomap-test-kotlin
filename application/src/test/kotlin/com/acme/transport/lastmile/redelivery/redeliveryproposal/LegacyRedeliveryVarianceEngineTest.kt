package com.acme.transport.lastmile.redelivery.redeliveryproposal

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyRedeliveryVarianceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyRedeliveryVarianceEngine(newRegistry())
        val item = newLegacyRedeliveryVariance("code-223", "name-169")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
