package com.acme.transport.crewing.restbreak.breakpreference

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TieredRestBreakPreferenceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredRestBreakPreferenceEngine(newRegistry())
        val item = newTieredRestBreakPreference("code-504", "label-351")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
