package com.acme.transport.terminal.dwell.notice

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DwellScoreEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDwellScoreEngine(newRegistry())
        val item = newDwellScore("code-787", "label-258")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
