package com.acme.transport.crewing.restbreak.tier

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RestBreakTierEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRestBreakTierEngine(newRegistry())
        val item = newRestBreakTier("code-727", "name-693")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
