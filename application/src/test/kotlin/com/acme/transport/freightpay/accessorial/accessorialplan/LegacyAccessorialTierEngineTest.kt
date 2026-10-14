package com.acme.transport.freightpay.accessorial.accessorialplan

import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyAccessorialTierEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyAccessorialTierEngine()
        val item = newLegacyAccessorialTier("code-288", "title-752")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
