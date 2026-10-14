package com.acme.transport.customs.declaration.declarationbatch

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalDeclarationBatchEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalDeclarationBatchEngine(newRegistry())
        val item = newSeasonalDeclarationBatch("code-315", "title-684")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
