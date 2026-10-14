package com.acme.transport.terminal.dwell.group

import kotlin.test.Test
import kotlin.test.assertTrue

class ManualDwellSegmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualDwellSegmentEngine()
        val item = newManualDwellSegment("code-207", "name-920")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
