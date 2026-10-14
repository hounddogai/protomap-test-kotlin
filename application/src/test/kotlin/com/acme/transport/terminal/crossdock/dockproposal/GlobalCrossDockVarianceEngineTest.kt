package com.acme.transport.terminal.crossdock.dockproposal

import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalCrossDockVarianceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalCrossDockVarianceEngine()
        val item = newGlobalCrossDockVariance("code-194", "title-948")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
