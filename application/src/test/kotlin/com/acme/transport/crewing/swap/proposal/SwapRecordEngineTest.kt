package com.acme.transport.crewing.swap.proposal

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class SwapRecordEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSwapRecordEngine(newRegistry())
        val item = newSwapRecord("code-731", "label-897")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
