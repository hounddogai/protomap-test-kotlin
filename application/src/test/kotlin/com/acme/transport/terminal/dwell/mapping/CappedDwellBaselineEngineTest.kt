package com.acme.transport.terminal.dwell.mapping

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CappedDwellBaselineEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedDwellBaselineEngine(newRegistry())
        val item = newCappedDwellBaseline("code-598", "label-796")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
