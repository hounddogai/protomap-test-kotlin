package com.acme.transport.network.zone.segment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ZoneSegmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultZoneSegmentEngine(newRegistry())
        val item = newZoneSegment("code-226", "name-181")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
