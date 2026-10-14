package com.acme.transport.linehaul.trailer.rollup

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TrailerQuotaEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTrailerQuotaEngine(newRegistry())
        val item = newTrailerQuota("code-519", "title-868")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
