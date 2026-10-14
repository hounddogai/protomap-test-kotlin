package com.acme.transport.crewing.availability.seasonalavailabilitydigest

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TieredAvailabilityPolicyEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredAvailabilityPolicyEngine(newRegistry())
        val item = newTieredAvailabilityPolicy("code-937", "title-378")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
