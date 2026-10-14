package com.acme.transport.crewing.swap.baseline

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class SwapNoticeEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSwapNoticeEngine(newRegistry())
        val item = newSwapNotice("code-419", "title-511")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
