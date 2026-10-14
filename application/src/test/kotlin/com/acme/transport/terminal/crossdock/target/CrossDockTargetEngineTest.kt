package com.acme.transport.terminal.crossdock.target

import kotlin.test.Test
import kotlin.test.assertTrue

class CrossDockTargetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCrossDockTargetEngine()
        val item = newCrossDockTarget("code-614", "name-882")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
