package com.acme.transport.linehaul.trailer.recorditem

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TrailerRecordEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTrailerRecordEngine(newRegistry())
        val item = newTrailerRecord("code-143", "label-948")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
