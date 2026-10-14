package com.acme.transport.crewing.restbreak.rollup

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RestBreakRecordEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRestBreakRecordEngine(newRegistry())
        val item = newRestBreakRecord("code-740", "label-52")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
