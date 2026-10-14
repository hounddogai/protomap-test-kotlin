package com.acme.transport.terminal.sortation.estimate

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class SortationEstimateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSortationEstimateEngine(newRegistry())
        val item = newSortationEstimate("code-432", "name-444")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
