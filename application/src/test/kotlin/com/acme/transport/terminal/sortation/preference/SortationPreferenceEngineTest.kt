package com.acme.transport.terminal.sortation.preference

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class SortationPreferenceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSortationPreferenceEngine(newRegistry())
        val item = newSortationPreference("code-859", "name-121")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
