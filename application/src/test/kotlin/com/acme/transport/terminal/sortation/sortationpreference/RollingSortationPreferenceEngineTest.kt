package com.acme.transport.terminal.sortation.sortationpreference

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RollingSortationPreferenceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingSortationPreferenceEngine(newRegistry())
        val item = newRollingSortationPreference("code-94", "title-75")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
