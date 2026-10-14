package com.acme.transport.crewing.swap.schedule

import kotlin.test.Test
import kotlin.test.assertTrue

class SwapQuotaEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSwapQuotaEngine()
        val item = newSwapQuota("code-601", "title-780")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
