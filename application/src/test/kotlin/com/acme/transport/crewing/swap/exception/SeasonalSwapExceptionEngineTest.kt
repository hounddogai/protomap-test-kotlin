package com.acme.transport.crewing.swap.exception

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalSwapExceptionEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalSwapExceptionEngine(newRegistry())
        val item = newSeasonalSwapException("code-246", "label-911")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
