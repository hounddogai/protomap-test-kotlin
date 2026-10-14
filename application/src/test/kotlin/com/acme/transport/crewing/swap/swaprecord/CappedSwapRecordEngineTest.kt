package com.acme.transport.crewing.swap.swaprecord

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CappedSwapRecordEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedSwapRecordEngine(newRegistry())
        val item = newCappedSwapRecord("code-689", "name-49")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
