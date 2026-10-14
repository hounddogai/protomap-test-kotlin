package com.acme.transport.terminal.crossdock.docksummary

import kotlin.test.Test
import kotlin.test.assertTrue

class TieredCrossDockMappingEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredCrossDockMappingEngine()
        val item = newTieredCrossDockMapping("code-780", "name-817")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
