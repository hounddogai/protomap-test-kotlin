package com.acme.transport.emissions.idling.manifest

import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultIdlingSegmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultIdlingSegmentEngine()
        val item = newDefaultIdlingSegment("code-469", "title-458")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
