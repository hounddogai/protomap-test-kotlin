package com.acme.transport.network.zone.batchedzonemapping

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledZoneSegmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledZoneSegmentEngine(newRegistry())
        val item = newScheduledZoneSegment("code-674", "title-346")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
