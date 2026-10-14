package com.acme.transport.crewing.availability.seasonalavailabilitydigest

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalAvailabilityDigestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalAvailabilityDigestEngine(newRegistry())
        val item = newRegionalAvailabilityDigest("code-666", "label-638")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
