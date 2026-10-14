package com.acme.transport.terminal.sortation.sortationbaseline

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledSortationBaselineEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledSortationBaselineEngine(newRegistry())
        val item = newScheduledSortationBaseline("code-763", "name-824")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
