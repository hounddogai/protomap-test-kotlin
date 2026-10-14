package com.acme.transport.terminal.dockdoor.assignment

import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultDockDoorSegmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultDockDoorSegmentEngine()
        val item = newDefaultDockDoorSegment("code-932", "title-287")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
