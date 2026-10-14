package com.acme.transport.network.cutoff.batch

import kotlin.test.Test
import kotlin.test.assertTrue

class StrictCutoffRollupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictCutoffRollupEngine()
        val item = newStrictCutoffRollup("code-354", "label-997")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
