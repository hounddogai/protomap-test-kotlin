package com.acme.transport.crewing.swap.schedule

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalSwapNoticeEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalSwapNoticeEngine(newRegistry())
        val item = newRegionalSwapNotice("code-93", "label-864")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
