package com.acme.transport.network.zone.threshold

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalZoneSegmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalZoneSegmentEngine(newRegistry())
        val item = newRegionalZoneSegment("code-470", "title-57")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
