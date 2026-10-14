package com.acme.transport.crewing.availability.preference

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TieredAvailabilityRollupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredAvailabilityRollupEngine(newRegistry())
        val item = newTieredAvailabilityRollup("code-710", "name-605")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
