package com.acme.transport.customs.declaration.estimate

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalDeclarationTierEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalDeclarationTierEngine(newRegistry())
        val item = newSeasonalDeclarationTier("code-241", "title-954")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
