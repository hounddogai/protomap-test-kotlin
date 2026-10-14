package com.acme.transport.terminal.sortation.variance

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class SortationVarianceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSortationVarianceEngine(newRegistry())
        val item = newSortationVariance("code-651", "name-876")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
